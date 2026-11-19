package testesoftware.com.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Item enviado no corpo de uma venda. O preço é sempre obtido do cadastro do produto.
 */
public record ItemVendaRequest(
        @NotNull(message = "O produtoId do item é obrigatório.") Long produtoId,
        @Positive(message = "A quantidade vendida deve ser maior que zero.") int qtdVendida) {
}
