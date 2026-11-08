package testesoftware.com.backend.enums;

/**
 * Enum que representa o status atual de uma venda.
 */
public enum StatusVenda {
    PENDENTE("Pendente"),
    FINALIZADA("Finalizada"),
    CANCELADO("Cancelado");

    private final String descricao;

    StatusVenda(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
