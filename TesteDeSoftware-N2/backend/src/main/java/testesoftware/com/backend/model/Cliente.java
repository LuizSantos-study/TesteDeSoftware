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
import testesoftware.com.backend.enums.PlanoCliente;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Entidade JPA que representa um Cliente da farmácia.
 */
@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do cliente é obrigatório.")
    @Column(nullable = false, length = 150)
    private String nome;

    @NotBlank(message = "O CPF do cliente é obrigatório.")
    @Column(nullable = false, unique = true, length = 14)
    private String cpf;

    @NotNull(message = "A data de nascimento do cliente é obrigatória.")
    @Column(name = "data_nasc", nullable = false)
    private LocalDate dataNasc;

    @NotNull(message = "O plano do cliente é obrigatório.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PlanoCliente plano;

    public Cliente() {
        this.plano = PlanoCliente.NENHUM;
    }

    public Cliente(Long id, String nome, String cpf, LocalDate dataNasc, PlanoCliente plano) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.dataNasc = dataNasc;
        this.plano = plano != null ? plano : PlanoCliente.NENHUM;
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

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public LocalDate getDataNasc() {
        return dataNasc;
    }

    public void setDataNasc(LocalDate dataNasc) {
        this.dataNasc = dataNasc;
    }

    public PlanoCliente getPlano() {
        return plano;
    }

    public void setPlano(PlanoCliente plano) {
        this.plano = plano;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cliente cliente = (Cliente) o;
        return Objects.equals(cpf, cliente.cpf);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cpf);
    }

    @Override
    public String toString() {
        return "Cliente{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", cpf='" + cpf + '\'' +
                ", dataNasc=" + dataNasc +
                ", plano=" + plano +
                '}';
    }
}
