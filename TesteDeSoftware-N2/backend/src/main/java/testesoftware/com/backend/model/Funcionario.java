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
import testesoftware.com.backend.enums.FuncaoFuncionario;

import java.util.Objects;

/**
 * Entidade JPA que representa um Funcionário da farmácia.
 */
@Entity
@Table(name = "funcionarios")
public class Funcionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do funcionário é obrigatório.")
    @Column(nullable = false, length = 150)
    private String nome;

    @NotBlank(message = "O CPF do funcionário é obrigatório.")
    @Column(nullable = false, unique = true, length = 14)
    private String cpf;

    @Column(length = 30)
    private String crf;

    @NotNull(message = "A função do funcionário é obrigatória.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FuncaoFuncionario funcao;

    public Funcionario() {
    }

    public Funcionario(Long id, String nome, String cpf, FuncaoFuncionario funcao) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.funcao = funcao;
    }

    public Funcionario(Long id, String nome, String cpf, String crf, FuncaoFuncionario funcao) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.crf = crf;
        this.funcao = funcao;
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

    public String getCrf() {
        return crf;
    }

    public void setCrf(String crf) {
        this.crf = crf;
    }

    public FuncaoFuncionario getFuncao() {
        return funcao;
    }

    public void setFuncao(FuncaoFuncionario funcao) {
        this.funcao = funcao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Funcionario that = (Funcionario) o;
        return Objects.equals(cpf, that.cpf);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cpf);
    }

    @Override
    public String toString() {
        return "Funcionario{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", cpf='" + cpf + '\'' +
                ", crf='" + crf + '\'' +
                ", funcao=" + funcao +
                '}';
    }
}
