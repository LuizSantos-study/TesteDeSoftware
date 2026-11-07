package testesoftware.com.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entidade JPA que representa um item individual pertencente a uma Venda.
 */
@Entity
@Table(name = "itens_venda")
public class ItemVenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_item")
    private Integer numeroItem;

    @NotNull(message = "O produto do item é obrigatório.")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venda_id")
    private Venda venda;

    @Positive(message = "A quantidade vendida deve ser maior que zero.")
    @Column(name = "qtd_vendida", nullable = false)
    private int qtdVendida;

    @NotNull(message = "O preço unitário é obrigatório.")
    @PositiveOrZero(message = "O preço unitário não pode ser negativo.")
    @Column(name = "preco_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoUnitario;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    public ItemVenda() {
        this.qtdVendida = 1;
        this.precoUnitario = BigDecimal.ZERO;
        this.subtotal = BigDecimal.ZERO;
    }

    public ItemVenda(Produto produto, int qtdVendida) {
        this.produto = produto;
        this.qtdVendida = qtdVendida;
        this.precoUnitario = produto != null ? produto.getPreco() : BigDecimal.ZERO;
        this.subtotal = BigDecimal.ZERO;
    }

    public ItemVenda(Produto produto, int qtdVendida, BigDecimal precoUnitario) {
        this.produto = produto;
        this.qtdVendida = qtdVendida;
        this.precoUnitario = precoUnitario;
        this.subtotal = BigDecimal.ZERO;
    }

    public ItemVenda(Long id, Integer numeroItem, Produto produto, Venda venda, int qtdVendida, BigDecimal precoUnitario, BigDecimal subtotal) {
        this.id = id;
        this.numeroItem = numeroItem;
        this.produto = produto;
        this.venda = venda;
        this.qtdVendida = qtdVendida;
        this.precoUnitario = precoUnitario;
        this.subtotal = subtotal;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumeroItem() {
        return numeroItem;
    }

    public void setNumeroItem(Integer numeroItem) {
        this.numeroItem = numeroItem;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public Venda getVenda() {
        return venda;
    }

    public void setVenda(Venda venda) {
        this.venda = venda;
    }

    public int getQtdVendida() {
        return qtdVendida;
    }

    public void setQtdVendida(int qtdVendida) {
        this.qtdVendida = qtdVendida;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(BigDecimal precoUnitario) {
        this.precoUnitario = precoUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemVenda itemVenda = (ItemVenda) o;
        return Objects.equals(id, itemVenda.id) &&
                Objects.equals(numeroItem, itemVenda.numeroItem) &&
                Objects.equals(produto, itemVenda.produto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, numeroItem, produto);
    }

    @Override
    public String toString() {
        return "ItemVenda{" +
                "id=" + id +
                ", numeroItem=" + numeroItem +
                ", produto=" + (produto != null ? produto.getNome() : null) +
                ", qtdVendida=" + qtdVendida +
                ", precoUnitario=" + precoUnitario +
                ", subtotal=" + subtotal +
                '}';
    }
}
