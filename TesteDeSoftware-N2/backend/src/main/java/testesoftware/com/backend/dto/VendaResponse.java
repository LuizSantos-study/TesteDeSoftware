package testesoftware.com.backend.dto;

import testesoftware.com.backend.enums.StatusVenda;
import testesoftware.com.backend.model.Venda;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Venda retornada pela API, com referências resumidas.
 */
public record VendaResponse(
        Long id,
        LocalDateTime dataHora,
        Long clienteId,
        String clienteNome,
        FuncionarioResponse funcionario,
        Long formaPagamentoId,
        String formaPagamentoDescricao,
        List<ItemVendaResponse> itens,
        BigDecimal desconto,
        BigDecimal valorTotal,
        StatusVenda status,
        String numeroReceita,
        FuncionarioResponse farmaceuticoResponsavel) {

    public static VendaResponse from(Venda v) {
        return new VendaResponse(v.getId(), v.getDataHora(),
                v.getCliente() != null ? v.getCliente().getId() : null,
                v.getCliente() != null ? v.getCliente().getNome() : null,
                FuncionarioResponse.from(v.getFuncionario()),
                v.getFormaPagamento() != null ? v.getFormaPagamento().getId() : null,
                v.getFormaPagamento() != null ? v.getFormaPagamento().getDescricao() : null,
                v.getItens().stream().map(ItemVendaResponse::from).toList(),
                v.getDesconto(), v.getValorTotal(), v.getStatus(), v.getNumeroReceita(),
                FuncionarioResponse.from(v.getFarmaceuticoResponsavel()));
    }
}
