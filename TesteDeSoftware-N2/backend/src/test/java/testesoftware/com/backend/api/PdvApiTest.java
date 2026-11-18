package testesoftware.com.backend.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import testesoftware.com.backend.enums.FuncaoFuncionario;
import testesoftware.com.backend.enums.MeioPagamento;
import testesoftware.com.backend.enums.ModalidadeIntegracao;
import testesoftware.com.backend.enums.PlanoCliente;
import testesoftware.com.backend.enums.TipoProduto;
import testesoftware.com.backend.model.Cliente;
import testesoftware.com.backend.model.FormaPagamento;
import testesoftware.com.backend.model.Funcionario;
import testesoftware.com.backend.model.Produto;
import testesoftware.com.backend.repository.ClienteRepository;
import testesoftware.com.backend.repository.FormaPagamentoRepository;
import testesoftware.com.backend.repository.FuncionarioRepository;
import testesoftware.com.backend.repository.ProdutoRepository;
import testesoftware.com.backend.repository.VendaRepository;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class PdvApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private VendaRepository vendaRepository;

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private FormaPagamentoRepository formaPagamentoRepository;

    private Long funcionarioId;
    private Long clienteId;
    private Long produtoId;
    private Long formaPagamentoId;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
        
        Funcionario func = new Funcionario();
        func.setNome("Lucas Lima");
        func.setCpf("444.555.666-77");
        func.setFuncao(FuncaoFuncionario.OPERADOR_CAIXA);
        func.setEmail("lucas.caixa@farmacia.com");
        func.setSenha("123456");
        funcionarioId = funcionarioRepository.save(func).getId();

        Cliente cliente = new Cliente();
        cliente.setNome("João da Silva");
        cliente.setCpf("111.222.333-44");
        cliente.setDataNasc(LocalDate.of(1985, 4, 12));
        cliente.setPlano(PlanoCliente.SAUDE);
        clienteId = clienteRepository.save(cliente).getId();

        Produto produto = new Produto();
        produto.setNome("Dipirona Monoidratada 500mg");
        produto.setPrincipioAtivo("Dipirona Monoidratada");
        produto.setTipo(TipoProduto.GENERICO);
        produto.setSngpc(false);
        produto.setPreco(new BigDecimal("8.50"));
        produtoId = produtoRepository.save(produto).getId();

        FormaPagamento fp = new FormaPagamento();
        fp.setDescricao("Dinheiro em Espécie");
        fp.setTipo(MeioPagamento.DINHEIRO);
        fp.setTipoPagamento(ModalidadeIntegracao.NAO_APLICAVEL);
        fp.setAtivo(true);
        fp.setPermiteParcelamento(false);
        formaPagamentoId = formaPagamentoRepository.save(fp).getId();
    }

    // Caso 1: Login autentica com sucesso e nunca expõe a senha na resposta
    @Test
    void deveAutenticarFuncionarioSemRetornarSenha() {
        String loginJson = """
            {
              "email": "lucas.caixa@farmacia.com",
              "senha": "123456"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(loginJson)
        .when()
            .post("/funcionarios/login")
        .then()
            .statusCode(200)
            .body("autenticado", equalTo(true))
            .body("funcionario.email", equalTo("lucas.caixa@farmacia.com"))
            .body("funcionario", not(hasKey("senha")));
    }

    // Caso 2: Fluxo completo de venda (Criar -> Finalizar -> Bloquear alteração pós-finalização)
    @Test
    void deveCriarFinalizarEBloquearAlteracaoDeVendaFinalizada() {
        String vendaJson = """
            {
              "clienteId": %d,
              "funcionarioId": %d,
              "formaPagamentoId": %d,
              "desconto": 0,
              "itens": [
                { "produtoId": %d, "qtdVendida": 2 }
              ]
            }
            """.formatted(clienteId, funcionarioId, formaPagamentoId, produtoId);

        // 1. Cria venda (nasce PENDENTE)
        int vendaId = given()
            .contentType(ContentType.JSON)
            .body(vendaJson)
        .when()
            .post("/vendas")
        .then()
            .statusCode(anyOf(is(200), is(201)))
            .body("status", equalTo("PENDENTE"))
            .extract()
            .path("id");

        // 2. Finaliza venda
        given()
        .when()
            .put("/vendas/{id}/finalizar", vendaId)
        .then()
            .statusCode(200)
            .body("status", equalTo("FINALIZADA"));

        // 3. Tenta aplicar desconto após finalizada -> deve bloquear
        given()
            .contentType(ContentType.JSON)
            .body("{ \"desconto\": 5.00 }")
        .when()
            .put("/vendas/{id}/desconto", vendaId)
        .then()
            .statusCode(anyOf(is(400), is(409), is(422)));
    }
}