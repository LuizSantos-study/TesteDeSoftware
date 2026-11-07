package testesoftware.com.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import testesoftware.com.backend.enums.FuncaoFuncionario;
import testesoftware.com.backend.enums.ModalidadeIntegracao;
import testesoftware.com.backend.enums.StatusVenda;
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
 * - Cancelamento de venda;
 * - Validação sanitária de medicamentos controlados (SNGPC / ANVISA);
 * - Integração com Gateway de Pagamento (TEF / POS) e SNGPC.
 */
@Service
public class VendaService {

    private final VendaRepository vendaRepository;
    private final GatewayPagamentoClient gatewayPagamentoClient;
    private final SngpcClient sngpcClient;

    public VendaService(VendaRepository vendaRepository,
                        GatewayPagamentoClient gatewayPagamentoClient,
                        SngpcClient sngpcClient) {
        this.vendaRepository = vendaRepository;
        this.gatewayPagamentoClient = gatewayPagamentoClient;
        this.sngpcClient = sngpcClient;
    }

    // =========================================================================
    // REGRAS DE NEGÓCIO DE ITEM VENDA E VENDA
    // =========================================================================

    /**
     * Regra 1: Calcula automaticamente o subtotal do item (precoUnitario * qtdVendida).
     * Impede quantidade vendida menor ou igual a zero ou preço negativo.
     */
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

    /**
     * Inicia uma nova venda pendente com dados básicos.
     */
    public Venda iniciarVenda(Cliente cliente, Funcionario funcionario, FormaPagamento formaPagamento) {
        return new Venda(null, cliente, funcionario, formaPagamento);
    }

    /**
     * Adiciona um produto à venda instanciando um ItemVenda com subtotal calculado
     * e recalculando o valor total da venda.
     * Bloqueia a operação caso a venda já esteja FINALIZADA.
     */
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

    /**
     * Adiciona um objeto ItemVenda já instanciado à venda.
     * Bloqueia a operação caso a venda já esteja FINALIZADA.
     */
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

    /**
     * Cancela / remove um item da venda e recalcula o valor total.
     * Bloqueia o cancelamento caso a venda já tenha sido finalizada.
     */
    public boolean cancelarItem(Venda venda, ItemVenda item) {
        validarVendaNaoFinalizada(venda);

        boolean removido = venda.getItens().remove(item);
        if (removido) {
            calcularValorTotal(venda);
        }
        return removido;
    }

    /**
     * Cancela um item da venda localizando-o pelo ID do item ou pelo número sequencial do item no carrinho.
     * Bloqueia o cancelamento caso a venda já esteja FINALIZADA.
     */
    @Transactional
    public Venda cancelarItemPorId(Long vendaId, Long itemId) {
        Venda venda = buscarPorId(vendaId);
        validarVendaNaoFinalizada(venda);

        ItemVenda itemParaCancelar = venda.getItens().stream()
                .filter(i -> itemId.equals(i.getId())
                        || (i.getNumeroItem() != null && itemId.equals(i.getNumeroItem().longValue())))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item não encontrado na venda com identificador: " + itemId));

        cancelarItem(venda, itemParaCancelar);
        return vendaRepository.save(venda);
    }

    /**
     * Alias de compatibilidade para cancelarItem.
     */
    public boolean removerItem(Venda venda, ItemVenda item) {
        return cancelarItem(venda, item);
    }

    /**
     * Calcula o somatório dos subtotais de todos os itens da venda.
     */
    public BigDecimal calcularSubtotalItens(Venda venda) {
        if (venda == null || venda.getItens() == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        return venda.getItens().stream()
                .filter(Objects::nonNull)
                .map(item -> {
                    if (item.getSubtotal() == null || item.getSubtotal().compareTo(BigDecimal.ZERO) == 0) {
                        return calcularSubtotalItem(item);
                    }
                    return item.getSubtotal();
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Regra 4: Aplica um valor de desconto à venda.
     * Impede descontos negativos ou que excedam o subtotal dos itens (impedindo valor total negativo).
     */
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

    /**
     * Recalcula o valor total da venda considerando o subtotal dos itens e o desconto.
     * Garante que o valor total nunca seja negativo.
     */
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

    /**
     * Verifica se existe algum item controlado (SNGPC) na venda.
     */
    public boolean possuiMedicamentoControlado(Venda venda) {
        if (venda == null || venda.getItens() == null) {
            return false;
        }
        return venda.getItens().stream()
                .map(ItemVenda::getProduto)
                .filter(Objects::nonNull)
                .anyMatch(Produto::isSngpc);
    }

    /**
     * Regra 2: Valida os requisitos sanitários para venda de medicamentos controlados (SNGPC):
     * - Presença do número da receita médica;
     * - Validação por profissional farmacêutico (funcionário da venda ou farmacêutico responsável).
     */
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

    /**
     * Regra 3: Bloqueia alterações caso a venda já esteja com status FINALIZADA.
     */
    public void validarVendaNaoFinalizada(Venda venda) {
        if (venda == null) {
            throw new IllegalArgumentException("A venda não pode ser nula.");
        }
        if (venda.getStatus() == StatusVenda.FINALIZADA) {
            throw new VendaFinalizadaException("Operação não permitida: a venda já está com status FINALIZADA.");
        }
    }

    /**
     * Valida todas as regras internas e altera o status da venda para FINALIZADA.
     */
    public void finalizarVenda(Venda venda) {
        validarVendaNaoFinalizada(venda);

        if (venda.getStatus() == StatusVenda.CANCELADA) {
            throw new IllegalStateException("Não é possível finalizar uma venda que já está cancelada.");
        }

        if (venda.getItens() == null || venda.getItens().isEmpty()) {
            throw new IllegalStateException("Não é possível finalizar uma venda sem itens.");
        }

        validarMedicamentoControlado(venda);
        calcularValorTotal(venda);

        venda.setStatus(StatusVenda.FINALIZADA);
    }

    /**
     * Cancela uma venda que não esteja finalizada nem já cancelada.
     */
    public void cancelarVenda(Venda venda) {
        validarVendaNaoFinalizada(venda);
        if (venda.getStatus() == StatusVenda.CANCELADA) {
            throw new IllegalStateException("A venda já está cancelada.");
        }
        venda.setStatus(StatusVenda.CANCELADA);
    }

    /**
     * Cancela uma venda pelo seu ID e persiste a alteração no banco.
     */
    @Transactional
    public Venda cancelarVendaPorId(Long vendaId) {
        Venda venda = buscarPorId(vendaId);
        cancelarVenda(venda);
        return vendaRepository.save(venda);
    }

    /**
     * Verifica se a forma de pagamento exige integração eletrônica (TEF ou POS).
     */
    public boolean requerIntegracaoEletronica(FormaPagamento formaPagamento) {
        if (formaPagamento == null || formaPagamento.getTipoPagamento() == null) {
            return false;
        }
        return formaPagamento.getTipoPagamento() == ModalidadeIntegracao.TEF
                || formaPagamento.getTipoPagamento() == ModalidadeIntegracao.POS;
    }

    /**
     * Processa a finalização completa da venda com integrações externas e persistência:
     * 1. Validação de venda não finalizada e presença de itens;
     * 2. Consulta e emissão de notificação no SNGPC (se contiver controlados);
     * 3. Autorização de pagamento via TEF/POS (se modalidade eletrônica);
     * 4. Transição de status da venda para FINALIZADA;
     * 5. Persistência da venda no repositório JPA.
     */
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
                    .filter(item -> item.getProduto() != null && item.getProduto().isSngpc())
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

    // =========================================================================
    // OPERAÇÕES DE PERSISTÊNCIA / CRUD NO REPOSITÓRIO JPA
    // =========================================================================

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
    public Venda atualizar(Long id, Venda dadosAtualizados) {
        Venda existente = buscarPorId(id);
        validarVendaNaoFinalizada(existente);

        existente.setCliente(dadosAtualizados.getCliente());
        existente.setFuncionario(dadosAtualizados.getFuncionario());
        existente.setFormaPagamento(dadosAtualizados.getFormaPagamento());
        existente.setNumeroReceita(dadosAtualizados.getNumeroReceita());
        existente.setFarmaceuticoResponsavel(dadosAtualizados.getFarmaceuticoResponsavel());

        if (dadosAtualizados.getItens() != null) {
            existente.getItens().clear();
            for (int i = 0; i < dadosAtualizados.getItens().size(); i++) {
                ItemVenda item = dadosAtualizados.getItens().get(i);
                item.setVenda(existente);
                item.setNumeroItem(i + 1);
                calcularSubtotalItem(item);
                existente.getItens().add(item);
            }
        }

        aplicarDesconto(existente, dadosAtualizados.getDesconto());

        if (dadosAtualizados.getStatus() == StatusVenda.FINALIZADA) {
            return processarFinalizacaoVenda(existente);
        } else if (dadosAtualizados.getStatus() == StatusVenda.CANCELADA) {
            cancelarVenda(existente);
        }

        return vendaRepository.save(existente);
    }

    @Transactional
    public Venda salvar(Venda venda) {
        calcularValorTotal(venda);
        return vendaRepository.save(venda);
    }

    @Transactional
    public void deletar(Long id) {
        Venda existente = buscarPorId(id);
        validarVendaNaoFinalizada(existente);
        vendaRepository.delete(existente);
    }
}
