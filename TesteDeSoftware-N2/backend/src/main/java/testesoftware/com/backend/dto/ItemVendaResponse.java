package testesoftware.com.backend.dto;

import testesoftware.com.backend.enums.StatusItemVenda;
import testesoftware.com.backend.model.ItemVenda;

import java.math.BigDecimal;

/**
 * Item de venda retornado pela API (inclui itens CANCELADO, mantidos para auditoria).
 */
public record ItemVendaResponse(
        Long id,
        Integer numeroItem,
        Long produtoId,
        String produtoNome,
        int qtdVendida,
        BigDecimal precoUnitario,
        BigDecimal subtotal,
        StatusItemVenda status) {

    public static ItemVendaResponse from(ItemVenda i) {
        return new ItemVendaResponse(i.getId(), i.getNumeroItem(),
                i.getProduto() != null ? i.getProduto().getId() : null,
                i.getProduto() != null ? i.getProduto().getNome() : null,
                i.getQtdVendida(), i.getPrecoUnitario(), i.getSubtotal(), i.getStatus());
    }
}
