package testesoftware.com.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Corpo de POST /api/vendas/{id}/itens. O preço é obtido do cadastro do produto.
 */
public record AdicionarItemRequest(
        @NotNull(message = "O produtoId é obrigatório.") Long produtoId,
        @Positive(message = "A quantidade deve ser maior que zero.") Integer quantidade) {

    public AdicionarItemRequest {
        if (quantidade == null) {
            quantidade = 1;
        }
    }
}
