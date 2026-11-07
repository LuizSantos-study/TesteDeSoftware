package testesoftware.com.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import testesoftware.com.backend.enums.TipoProduto;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entidade JPA que representa um Produto comercializado na farmácia.
 */
@Entity
@Table(name = "produtos")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do produto é obrigatório.")
    @Column(nullable = false, length = 150)
    private String nome;

    @NotBlank(message = "O princípio ativo é obrigatório.")
    @Column(name = "principio_ativo", nullable = false, length = 150)
    private String principioAtivo;

    @NotNull(message = "O tipo do produto é obrigatório.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoProduto tipo;

    @Column(nullable = false)
    private boolean sngpc;

    @NotNull(message = "O preço do produto é obrigatório.")
    @PositiveOrZero(message = "O preço do produto não pode ser negativo.")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    public Produto() {
        this.preco = BigDecimal.ZERO;
    }

    public Produto(Long id, String nome, String principioAtivo, TipoProduto tipo, boolean sngpc, BigDecimal preco) {
        this.id = id;
        this.nome = nome;
        this.principioAtivo = principioAtivo;
        this.tipo = tipo;
        this.sngpc = sngpc;
        this.preco = preco;
    }

    public Produto(Long id, String nome, String principioAtivo, TipoProduto tipo, boolean sngpc, double preco) {
        this(id, nome, principioAtivo, tipo, sngpc, BigDecimal.valueOf(preco));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getPrincipioAtivo() {
        return principioAtivo;
    }

    public void setPrincipioAtivo(String principioAtivo) {
        this.principioAtivo = principioAtivo;
    }

    public TipoProduto getTipo() {
        return tipo;
    }

    public void setTipo(TipoProduto tipo) {
        this.tipo = tipo;
    }

    public boolean isSngpc() {
        return sngpc;
    }

    public void setSngpc(boolean sngpc) {
        this.sngpc = sngpc;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public void setPreco(double preco) {
        this.preco = BigDecimal.valueOf(preco);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Produto produto = (Produto) o;
        return Objects.equals(id, produto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Produto{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", principioAtivo='" + principioAtivo + '\'' +
                ", tipo=" + tipo +
                ", sngpc=" + sngpc +
                ", preco=" + preco +
                '}';
    }
}
