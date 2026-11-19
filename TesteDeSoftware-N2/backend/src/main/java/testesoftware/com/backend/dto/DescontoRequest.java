package testesoftware.com.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * Corpo de PUT /api/vendas/{id}/desconto.
 */
public record DescontoRequest(
        @NotNull(message = "O desconto é obrigatório.")
        @PositiveOrZero(message = "O desconto não pode ser negativo.") BigDecimal desconto) {
}
