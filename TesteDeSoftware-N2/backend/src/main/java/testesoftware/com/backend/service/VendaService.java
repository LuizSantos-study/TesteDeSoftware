package testesoftware.com.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import testesoftware.com.backend.enums.FuncaoFuncionario;
import testesoftware.com.backend.enums.ModalidadeIntegracao;
import testesoftware.com.backend.enums.StatusItemVenda;
import testesoftware.com.backend.enums.StatusVenda;
import testesoftware.com.backend.dto.ItemVendaRequest;
import testesoftware.com.backend.dto.VendaRequest;
import testesoftware.com.backend.exception.DescontoInvalidoException;
import testesoftware.com.backend.exception.MedicamentoControladoException;
import testesoftware.com.backend.exception.VendaFinalizadaException;
import testesoftware.com.backend.model.Cliente;
import testesoftware.com.backend.model.FormaPagamento;
import testesoftware.com.backend.model.Funcionario;
import testesoftware.com.backend.model.ItemVenda;
import testesoftware.com.backend.model.Produto;
import testesoftware.com.backend.model.Venda;
import testesoftware.com.backend.repository.VendaRepository;
import testesoftware.com.backend.service.integracao.GatewayPagamentoClient;
import testesoftware.com.backend.service.integracao.SngpcClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Camada de serviço responsável por todas as regras de negócio de Venda e ItemVenda no PDV:
 * - Cálculo automático do subtotal do item e do valor total da venda;
 * - Adição e cancelamento de itens da venda;
 * - Aplicação de descontos e bloqueio de valor total negativo;
 * - Bloqueio de alterações em vendas com status FINALIZADA;
 * - Cancelamento lógico de venda e itens (sem exclusão física, para auditoria);
 * - Validação sanitária de medicamentos controlados (SNGPC / ANVISA);
 * - Integração com Gateway de Pagamento (TEF / POS) e SNGPC.
 */
@Service
public class VendaService {

    private final VendaRepository vendaRepository;
    private final GatewayPagamentoClient gatewayPagamentoClient;
    private final SngpcClient sngpcClient;
    private final ClienteService clienteService;
    private final FuncionarioService funcionarioService;
    private final FormaPagamentoService formaPagamentoService;
    private final ProdutoService produtoService;

    public VendaService(VendaRepository vendaRepository,
                        GatewayPagamentoClient gatewayPagamentoClient,
                        SngpcClient sngpcClient,
                        ClienteService clienteService,
                        FuncionarioService funcionarioService,
                        FormaPagamentoService formaPagamentoService,
                        ProdutoService produtoService) {
        this.clienteService = clienteService;
        this.funcionarioService = funcionarioService;
        this.formaPagamentoService = formaPagamentoService;
        this.produtoService = produtoService;
        this.vendaRepository = vendaRepository;
        this.gatewayPagamentoClient = gatewayPagamentoClient;
        this.sngpcClient = sngpcClient;
    }

    // Subtotal = preço unitário x quantidade
    public BigDecimal calcularSubtotalItem(ItemVenda item) {
        if (item == null) {
            throw new IllegalArgumentException("O item da venda não pode ser nulo.");
        }
        if (item.getQtdVendida() <= 0) {
            throw new IllegalArgumentException("A quantidade vendida deve ser maior que zero.");
        }
        if ((item.getPrecoUnitario() == null || item.getPrecoUnitario().compareTo(BigDecimal.ZERO) == 0)
                && item.getProduto() != null && item.getProduto().getPreco() != null) {
            item.setPrecoUnitario(item.getProduto().getPreco());
        }
        if (item.getPrecoUnitario() == null || item.getPrecoUnitario().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O preço unitário não pode ser nulo ou negativo.");
        }

        BigDecimal subtotal = item.getPrecoUnitario()
                .multiply(BigDecimal.valueOf(item.getQtdVendida()))
                .setScale(2, RoundingMode.HALF_UP);

        item.setSubtotal(subtotal);
        return subtotal;
    }

    public Venda iniciarVenda(Cliente cliente, Funcionario funcionario, FormaPagamento formaPagamento) {
        return new Venda(null, cliente, funcionario, formaPagamento);
    }

    public ItemVenda adicionarItem(Venda venda, Produto produto, int quantidade) {
        validarVendaNaoFinalizada(venda);

        if (produto == null) {
            throw new IllegalArgumentException("O item deve conter um produto válido.");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade vendida deve ser superior a zero.");
        }

        ItemVenda item = new ItemVenda(produto, quantidade, produto.getPreco());
        calcularSubtotalItem(item);

        item.setVenda(venda);
        if (item.getNumeroItem() == null) {
            item.setNumeroItem(venda.getItens().size() + 1);
        }

        venda.getItens().add(item);
        calcularValorTotal(venda);
        return item;
    }

    public void adicionarItem(Venda venda, ItemVenda item) {
        validarVendaNaoFinalizada(venda);

        if (item == null) {
            throw new IllegalArgumentException("O item da venda não pode ser nulo.");
        }
        if (item.getProduto() == null) {
            throw new IllegalArgumentException("O item deve conter um produto válido.");
        }
        if (item.getQtdVendida() <= 0) {
            throw new IllegalArgumentException("A quantidade vendida deve ser superior a zero.");
        }

        calcularSubtotalItem(item);
        item.setVenda(venda);
        if (item.getNumeroItem() == null) {
            item.setNumeroItem(venda.getItens().size() + 1);
        }

        venda.getItens().add(item);
        calcularValorTotal(venda);
    }

    public boolean cancelarItem(Venda venda, ItemVenda item) {
        validarVendaNaoFinalizada(venda);
        validarVendaNaoCancelada(venda);

        if (item == null || !venda.getItens().contains(item) || !item.isAtivo()) {
            return false;
        }
        // Cancelamento lógico: o item permanece para auditoria
        item.setStatus(StatusItemVenda.CANCELADO);
        calcularValorTotal(venda);
        return true;
    }

    @Transactional
    public Venda cancelarItemPorId(Long vendaId, Long itemId) {
        Venda venda = buscarPorId(vendaId);
        validarVendaNaoFinalizada(venda);

        ItemVenda itemParaCancelar = venda.getItens().stream()
                .filter(i -> itemId.equals(i.getId())
                        || (i.isAtivo() && i.getNumeroItem() != null && itemId.equals(i.getNumeroItem().longValue())))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item não encontrado na venda com identificador: " + itemId));

        if (!cancelarItem(venda, itemParaCancelar)) {
            throw new IllegalStateException("O item " + itemId + " já está cancelado.");
        }
        return vendaRepository.save(venda);
    }

    // Alias de compatibilidade para cancelarItem
    public boolean removerItem(Venda venda, ItemVenda item) {
        return cancelarItem(venda, item);
    }

    public BigDecimal calcularSubtotalItens(Venda venda) {
        if (venda == null || venda.getItens() == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        return venda.getItens().stream()
                .filter(Objects::nonNull)
                .filter(ItemVenda::isAtivo) // itens CANCELADO ficam registrados, mas não entram no total
                .map(item -> {
                    if (item.getSubtotal() == null || item.getSubtotal().compareTo(BigDecimal.ZERO) == 0) {
                        return calcularSubtotalItem(item);
                    }
                    return item.getSubtotal();
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    // Desconto não pode ser negativo nem maior que o subtotal
    public BigDecimal aplicarDesconto(Venda venda, BigDecimal novoDesconto) {
        validarVendaNaoFinalizada(venda);

        if (novoDesconto == null) {
            venda.setDesconto(BigDecimal.ZERO);
            return calcularValorTotal(venda);
        }

        if (novoDesconto.compareTo(BigDecimal.ZERO) < 0) {
            throw new DescontoInvalidoException("O valor do desconto não pode ser negativo.");
        }

        BigDecimal subtotal = calcularSubtotalItens(venda);
        if (novoDesconto.compareTo(subtotal) > 0) {
            throw new DescontoInvalidoException(
                    "O valor do desconto (R$ " + novoDesconto + ") não pode ser maior que o subtotal dos itens (R$ " + subtotal + ")."
            );
        }

        venda.setDesconto(novoDesconto.setScale(2, RoundingMode.HALF_UP));
        return calcularValorTotal(venda);
    }

    public BigDecimal calcularValorTotal(Venda venda) {
        BigDecimal subtotal = calcularSubtotalItens(venda);
        BigDecimal desc = venda.getDesconto() != null ? venda.getDesconto() : BigDecimal.ZERO;

        if (desc.compareTo(BigDecimal.ZERO) < 0) {
            throw new DescontoInvalidoException("O valor do desconto não pode ser negativo.");
        }

        BigDecimal total = subtotal.subtract(desc);

        if (total.compareTo(BigDecimal.ZERO) < 0) {
            throw new DescontoInvalidoException("O valor total da venda não pode ser negativo.");
        }

        BigDecimal totalFormatado = total.setScale(2, RoundingMode.HALF_UP);
        venda.setValorTotal(totalFormatado);
        return totalFormatado;
    }

    public boolean possuiMedicamentoControlado(Venda venda) {
        if (venda == null || venda.getItens() == null) {
            return false;
        }
        return venda.getItens().stream()
                .filter(ItemVenda::isAtivo)
                .map(ItemVenda::getProduto)
                .filter(Objects::nonNull)
                .anyMatch(Produto::isSngpc);
    }

    // Controlados exigem receita e farmacêutico
    public void validarMedicamentoControlado(Venda venda) {
        if (possuiMedicamentoControlado(venda)) {
            if (venda.getNumeroReceita() == null || venda.getNumeroReceita().trim().isEmpty()) {
                throw new MedicamentoControladoException(
                        "Venda contém medicamento controlado (SNGPC) e exige o número da receita médica."
                );
            }

            boolean funcionarioEhFarmaceutico = (venda.getFuncionario() != null &&
                    venda.getFuncionario().getFuncao() == FuncaoFuncionario.FARMACEUTICO);

            boolean farmaceuticoResponsavelValido = (venda.getFarmaceuticoResponsavel() != null &&
                    venda.getFarmaceuticoResponsavel().getFuncao() == FuncaoFuncionario.FARMACEUTICO);

            if (!funcionarioEhFarmaceutico && !farmaceuticoResponsavelValido) {
                throw new MedicamentoControladoException(
                        "Venda de medicamento controlado exige a validação/presença de um profissional Farmacêutico."
                );
            }
        }
    }

    // Venda finalizada não pode ser alterada
    public void validarVendaNaoFinalizada(Venda venda) {
        if (venda == null) {
            throw new IllegalArgumentException("A venda não pode ser nula.");
        }
        if (venda.getStatus() == StatusVenda.FINALIZADA) {
            throw new VendaFinalizadaException("Operação não permitida: a venda já está com status FINALIZADA.");
        }
    }

    // Bloqueia alterações em vendas já canceladas
    public void validarVendaNaoCancelada(Venda venda) {
        if (venda != null && venda.getStatus() == StatusVenda.CANCELADO) {
            throw new IllegalStateException("Operação não permitida: a venda está cancelada.");
        }
    }

    // Valida e marca a venda como FINALIZADA
    public void finalizarVenda(Venda venda) {
        validarVendaNaoFinalizada(venda);

        if (venda.getStatus() == StatusVenda.CANCELADO) {
            throw new IllegalStateException("Não é possível finalizar uma venda que já está cancelada.");
        }

        if (venda.getItens() == null || venda.getItens().stream().noneMatch(ItemVenda::isAtivo)) {
            throw new IllegalStateException("Não é possível finalizar uma venda sem itens ativos.");
        }

        validarMedicamentoControlado(venda);
        calcularValorTotal(venda);

        venda.setStatus(StatusVenda.FINALIZADA);
    }

    public void cancelarVenda(Venda venda) {
        validarVendaNaoFinalizada(venda);
        if (venda.getStatus() == StatusVenda.CANCELADO) {
            throw new IllegalStateException("A venda já está cancelada.");
        }
        // Venda e itens continuam no banco
        venda.setStatus(StatusVenda.CANCELADO);
    }

    @Transactional
    public Venda cancelarVendaPorId(Long vendaId) {
        Venda venda = buscarPorId(vendaId);
        cancelarVenda(venda);
        return vendaRepository.save(venda);
    }

    public boolean requerIntegracaoEletronica(FormaPagamento formaPagamento) {
        if (formaPagamento == null || formaPagamento.getTipoPagamento() == null) {
            return false;
        }
        return formaPagamento.getTipoPagamento() == ModalidadeIntegracao.TEF
                || formaPagamento.getTipoPagamento() == ModalidadeIntegracao.POS;
    }

    @Transactional
    public Venda processarFinalizacaoVenda(Venda venda) {
        validarVendaNaoFinalizada(venda);

        // 1. Tratamento de medicamentos controlados (SNGPC / ANVISA)
        if (possuiMedicamentoControlado(venda)) {
            validarMedicamentoControlado(venda);

            String cpfCliente = (venda.getCliente() != null) ? venda.getCliente().getCpf() : null;
            boolean receitaValida = sngpcClient.consultarReceita(venda.getNumeroReceita(), cpfCliente);

            if (!receitaValida) {
                throw new MedicamentoControladoException("Receita médica recusada ou inválida no sistema SNGPC.");
            }

            String crfFarmaceutico = (venda.getFarmaceuticoResponsavel() != null && venda.getFarmaceuticoResponsavel().getCrf() != null)
                    ? venda.getFarmaceuticoResponsavel().getCrf()
                    : (venda.getFuncionario() != null ? venda.getFuncionario().getCrf() : "");

            List<ItemVenda> itensControlados = venda.getItens().stream()
                    .filter(item -> item.isAtivo() && item.getProduto() != null && item.getProduto().isSngpc())
                    .collect(Collectors.toList());

            sngpcClient.emitirNotificacaoSNGPC(venda.getNumeroReceita(), crfFarmaceutico, itensControlados);
        }

        // 2. Recalcula valor total antes de solicitar autorização financeira
        calcularValorTotal(venda);

        // 3. Autorização de pagamento em caso de TEF ou POS
        if (requerIntegracaoEletronica(venda.getFormaPagamento())) {
            boolean autorizado = gatewayPagamentoClient.autorizarPagamento(venda.getFormaPagamento(), venda.getValorTotal());
            if (!autorizado) {
                throw new IllegalStateException("Pagamento via " + venda.getFormaPagamento().getTipoPagamento() + " não autorizado pela operadora.");
            }
        }

        // 4. Conclusão da venda e persistência
        finalizarVenda(venda);
        return vendaRepository.save(venda);
    }

    public List<Venda> listarTodas() {
        return vendaRepository.findAll();
    }

    public Venda buscarPorId(Long id) {
        return vendaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Venda não encontrada com ID: " + id));
    }

    @Transactional
    public Venda criar(Venda venda) {
        if (venda == null) {
            throw new IllegalArgumentException("Os dados da venda não podem ser nulos.");
        }
        if (venda.getDataHora() == null) {
            venda.setDataHora(LocalDateTime.now());
        }
        if (venda.getStatus() == null) {
            venda.setStatus(StatusVenda.PENDENTE);
        }
        if (venda.getDesconto() == null) {
            venda.setDesconto(BigDecimal.ZERO);
        }

        if (venda.getItens() != null) {
            for (int i = 0; i < venda.getItens().size(); i++) {
                ItemVenda item = venda.getItens().get(i);
                item.setVenda(venda);
                if (item.getNumeroItem() == null) {
                    item.setNumeroItem(i + 1);
                }
                calcularSubtotalItem(item);
            }
        }

        calcularValorTotal(venda);

        if (venda.getStatus() == StatusVenda.FINALIZADA) {
            venda.setStatus(StatusVenda.PENDENTE);
            return processarFinalizacaoVenda(venda);
        }

        return vendaRepository.save(venda);
    }

    @Transactional
    public Venda criar(VendaRequest request) {
        return criar(montarVenda(request));
    }

    @Transactional
    public Venda atualizar(Long id, VendaRequest request) {
        return atualizar(id, montarVenda(request));
    }

    private Venda montarVenda(VendaRequest request) {
        Venda venda = new Venda();
        venda.setCliente(request.clienteId() != null ? clienteService.buscarPorId(request.clienteId()) : null);
        // Entidades relacionadas são carregadas do banco pelos IDs
        venda.setFuncionario(funcionarioService.buscarPorId(request.funcionarioId()));
        venda.setFormaPagamento(formaPagamentoService.buscarPorId(request.formaPagamentoId()));
        venda.setNumeroReceita(request.numeroReceita());
        venda.setFarmaceuticoResponsavel(request.farmaceuticoResponsavelId() != null
                ? funcionarioService.buscarPorId(request.farmaceuticoResponsavelId()) : null);
        venda.setDesconto(request.desconto() != null ? request.desconto() : BigDecimal.ZERO);

        for (ItemVendaRequest itemReq : request.itens()) {
            ItemVenda item = new ItemVenda(produtoService.buscarPorId(itemReq.produtoId()), itemReq.qtdVendida());
            venda.getItens().add(item);
        }
        return venda;
    }

    @Transactional
    public Venda atualizar(Long id, Venda dadosAtualizados) {
        Venda existente = buscarPorId(id);
        validarVendaNaoFinalizada(existente);

        existente.setCliente(dadosAtualizados.getCliente());
        existente.setFuncionario(dadosAtualizados.getFuncionario());
        existente.setFormaPagamento(dadosAtualizados.getFormaPagamento());
        existente.setNumeroReceita(dadosAtualizados.getNumeroReceita());
        existente.setFarmaceuticoResponsavel(dadosAtualizados.getFarmaceuticoResponsavel());

        if (dadosAtualizados.getItens() != null) {
            sincronizarItens(existente, dadosAtualizados.getItens());
        }

        aplicarDesconto(existente, dadosAtualizados.getDesconto());

        if (dadosAtualizados.getStatus() == StatusVenda.FINALIZADA) {
            return processarFinalizacaoVenda(existente);
        } else if (dadosAtualizados.getStatus() == StatusVenda.CANCELADO) {
            cancelarVenda(existente);
        }

        return vendaRepository.save(existente);
    }

    private void sincronizarItens(Venda venda, List<ItemVenda> recebidos) {
        List<ItemVenda> existentes = venda.getItens();
        java.util.Set<ItemVenda> mantidos = new java.util.HashSet<>();
        int proximoNumero = existentes.stream()
                .map(ItemVenda::getNumeroItem).filter(Objects::nonNull)
                .mapToInt(Integer::intValue).max().orElse(0) + 1;

        for (ItemVenda recebido : recebidos) {
            // Sem id no corpo: reaproveita o item ativo do mesmo produto (evita duplicar itens no PUT)
            ItemVenda atual = existentes.stream()
                    .filter(i -> !mantidos.contains(i))
                    .filter(i -> recebido.getId() != null
                            ? recebido.getId().equals(i.getId())
                            : i.isAtivo() && i.getProduto() != null && recebido.getProduto() != null
                                && Objects.equals(i.getProduto().getId(), recebido.getProduto().getId()))
                    .findFirst().orElse(null);

            if (atual == null) {
                recebido.setId(null);
                recebido.setVenda(venda);
                recebido.setNumeroItem(proximoNumero++);
                if (recebido.getStatus() == null) {
                    recebido.setStatus(StatusItemVenda.ATIVO);
                }
                calcularSubtotalItem(recebido);
                existentes.add(recebido);
                mantidos.add(recebido);
            } else {
                atual.setProduto(recebido.getProduto());
                atual.setQtdVendida(recebido.getQtdVendida());
                atual.setPrecoUnitario(recebido.getPrecoUnitario());
                atual.setStatus(recebido.getStatus());
                calcularSubtotalItem(atual);
                mantidos.add(atual);
            }
        }

        // Itens ausentes da lista são cancelados, nunca excluídos
        for (ItemVenda existente : existentes) {
            if (!mantidos.contains(existente)) {
                existente.setStatus(StatusItemVenda.CANCELADO);
            }
        }
    }

    @Transactional
    public Venda salvar(Venda venda) {
        calcularValorTotal(venda);
        return vendaRepository.save(venda);
    }
}
