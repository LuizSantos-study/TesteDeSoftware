package testesoftware.com.backend.service.integracao;

import org.springframework.stereotype.Service;
import testesoftware.com.backend.model.FormaPagamento;

import java.math.BigDecimal;

/**
 * Implementação padrão do Gateway de Pagamento (TEF / POS) para execução da API.
 */
@Service
public class GatewayPagamentoClientImpl implements GatewayPagamentoClient {

    @Override
    public boolean autorizarPagamento(FormaPagamento formaPagamento, BigDecimal valor) {
        return formaPagamento != null
                && formaPagamento.isAtivo()
                && valor != null
                && valor.compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    public boolean estornarPagamento(String idTransacao, BigDecimal valor) {
        return idTransacao != null && !idTransacao.isBlank()
                && valor != null && valor.compareTo(BigDecimal.ZERO) > 0;
    }
}
