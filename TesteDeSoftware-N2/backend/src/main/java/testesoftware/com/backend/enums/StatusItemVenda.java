package testesoftware.com.backend.enums;

/**
 * Status de um item de venda. Itens cancelados nunca são excluídos (auditoria).
 */
public enum StatusItemVenda {
    ATIVO("Ativo"),
    CANCELADO("Cancelado");

    private final String descricao;

    StatusItemVenda(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
