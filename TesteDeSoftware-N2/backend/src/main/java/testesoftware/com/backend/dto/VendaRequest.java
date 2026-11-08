package testesoftware.com.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

/**
 * Corpo de criação/atualização de venda. Referencia as demais entidades somente por ID;
 * o ID da venda é gerado pelo banco e nunca é informado.
 */
public record VendaRequest(
        Long clienteId,
        @NotNull(message = "O funcionarioId é obrigatório.") Long funcionarioId,
        @NotNull(message = "A formaPagamentoId é obrigatória.") Long formaPagamentoId,
        BigDecimal desconto,
        String numeroReceita,
        Long farmaceuticoResponsavelId,
        @NotEmpty(message = "A venda deve ter ao menos um item.") List<@Valid ItemVendaRequest> itens) {
}
