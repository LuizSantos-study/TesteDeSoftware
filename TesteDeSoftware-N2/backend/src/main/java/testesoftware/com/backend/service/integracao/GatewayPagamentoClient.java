package testesoftware.com.backend.service.integracao;

import testesoftware.com.backend.model.FormaPagamento;

import java.math.BigDecimal;

/**
 * Interface de integração com o Gateway de Pagamento (TEF / POS).
 * Permite simular a autorização de pagamentos eletrônicos via Mock Objects (Mockito) nos testes.
 */
public interface GatewayPagamentoClient {

    boolean autorizarPagamento(FormaPagamento formaPagamento, BigDecimal valor);

    boolean estornarPagamento(String idTransacao, BigDecimal valor);
}
