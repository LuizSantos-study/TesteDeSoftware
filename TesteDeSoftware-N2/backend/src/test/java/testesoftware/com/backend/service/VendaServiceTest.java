package testesoftware.com.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import testesoftware.com.backend.enums.FuncaoFuncionario;
import testesoftware.com.backend.enums.StatusItemVenda;
import testesoftware.com.backend.enums.StatusVenda;
import testesoftware.com.backend.exception.DescontoInvalidoException;
import testesoftware.com.backend.exception.MedicamentoControladoException;
import testesoftware.com.backend.exception.VendaFinalizadaException;
import testesoftware.com.backend.model.Funcionario;
import testesoftware.com.backend.model.ItemVenda;
import testesoftware.com.backend.model.Produto;
import testesoftware.com.backend.model.Venda;
import testesoftware.com.backend.repository.VendaRepository;
import testesoftware.com.backend.service.integracao.GatewayPagamentoClient;
import testesoftware.com.backend.service.integracao.SngpcClient;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VendaServiceTest {

    @Mock
    private VendaRepository vendaRepository;

    @Mock
    private GatewayPagamentoClient gatewayPagamentoClient;

    @Mock
    private SngpcClient sngpcClient;

    @Mock
    private ClienteService clienteService;

    @Mock
    private FuncionarioService funcionarioService;

    @Mock
    private FormaPagamentoService formaPagamentoService;

    @Mock
    private ProdutoService produtoService;

    @InjectMocks
    private VendaService vendaService;

    // Caso 1: Desconto não pode ser maior que o subtotal nem negativo
    @Test
    void deveLancarExcecaoQuandoDescontoMaiorQueSubtotal() {
        Venda venda = new Venda();
        venda.setId(1L);
        venda.setStatus(StatusVenda.PENDENTE);

        Produto produto = new Produto();
        produto.setId(1L);
        produto.setPreco(new BigDecimal("50.00"));

        vendaService.adicionarItem(venda, produto, 2); // Subtotal = 100.00

        assertThrows(DescontoInvalidoException.class, () ->
            vendaService.aplicarDesconto(venda, new BigDecimal("150.00"))
        );
        verify(vendaRepository, never()).save(any());
    }

    // Caso 2: Medicamento controlado (SNGPC) exige número da receita e farmacêutico
    @Test
    void deveBloquearFinalizacaoDeControladoSemReceitaOuFarmaceutico() {
        Produto controlado = new Produto();
        controlado.setId(10L);
        controlado.setSngpc(true);
        controlado.setPreco(new BigDecimal("80.00"));

        Funcionario operador = new Funcionario();
        operador.setId(1L);
        operador.setFuncao(FuncaoFuncionario.OPERADOR_CAIXA);

        Venda venda = new Venda();
        venda.setId(1L);
        venda.setStatus(StatusVenda.PENDENTE);
        venda.setFuncionario(operador);
        venda.setNumeroReceita(null); // Sem receita médica

        vendaService.adicionarItem(venda, controlado, 1);

        assertThrows(MedicamentoControladoException.class, () ->
            vendaService.finalizarVenda(venda)
        );
    }

    // Caso 3: Auditoria (soft-delete) — itens cancelados permanecem na venda mas não entram no total
    @Test
    void deveCalcularTotalIgnorandoItensCancelados() {
        Venda venda = new Venda();
        venda.setId(1L);
        venda.setStatus(StatusVenda.PENDENTE);

        Produto prodAtivo = new Produto();
        prodAtivo.setId(1L);
        prodAtivo.setPreco(new BigDecimal("40.00"));

        Produto prodCancelado = new Produto();
        prodCancelado.setId(2L);
        prodCancelado.setPreco(new BigDecimal("100.00"));

        ItemVenda itemAtivo = vendaService.adicionarItem(venda, prodAtivo, 2);       // 80.00
        ItemVenda itemCancelado = vendaService.adicionarItem(venda, prodCancelado, 1); // 100.00

        boolean cancelado = vendaService.cancelarItem(venda, itemCancelado);
        BigDecimal total = vendaService.aplicarDesconto(venda, new BigDecimal("10.00"));

        assertTrue(cancelado);
        assertEquals(StatusItemVenda.CANCELADO, itemCancelado.getStatus());
        assertEquals(StatusItemVenda.ATIVO, itemAtivo.getStatus());
        assertEquals(2, venda.getItens().size()); // Continua persistido para auditoria
        assertEquals(0, new BigDecimal("70.00").compareTo(total));
    }

    // Caso 4: Venda finalizada não pode sofrer alterações
    @Test
    void deveBloquearAlteracaoEmVendaFinalizada() {
        Venda venda = new Venda();
        venda.setId(1L);
        venda.setStatus(StatusVenda.FINALIZADA);

        Produto produto = new Produto();
        produto.setId(1L);
        produto.setPreco(new BigDecimal("25.00"));

        assertThrows(VendaFinalizadaException.class, () ->
            vendaService.adicionarItem(venda, produto, 1)
        );

        assertThrows(VendaFinalizadaException.class, () ->
            vendaService.aplicarDesconto(venda, new BigDecimal("5.00"))
        );
    }
}