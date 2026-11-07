package testesoftware.com.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import testesoftware.com.backend.enums.FuncaoFuncionario;

import java.util.Objects;

/**
 * Entidade JPA que representa um Usuário de acesso ao sistema PDV da farmácia.
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do usuário é obrigatório.")
    @Column(nullable = false, length = 150)
    private String nome;

    @NotBlank(message = "O login do usuário é obrigatório.")
    @Column(nullable = false, unique = true, length = 60)
    private String login;

    @NotBlank(message = "O e-mail do usuário é obrigatório.")
    @Email(message = "O e-mail informado é inválido.")
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank(message = "A senha do usuário é obrigatória.")
    @Column(nullable = false, length = 150)
    private String senha;

    @NotNull(message = "O perfil/função do usuário é obrigatório.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FuncaoFuncionario perfil;

    @Column(nullable = false)
    private boolean ativo;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "funcionario_id")
    private Funcionario funcionario;

    public Usuario() {
        this.ativo = true;
        this.perfil = FuncaoFuncionario.OPERADOR_CAIXA;
    }

    public Usuario(Long id, String nome, String login, String email, String senha, FuncaoFuncionario perfil, boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.login = login;
        this.email = email;
        this.senha = senha;
        this.perfil = perfil != null ? perfil : FuncaoFuncionario.OPERADOR_CAIXA;
        this.ativo = ativo;
    }

    public Usuario(Long id, String nome, String login, String email, String senha, FuncaoFuncionario perfil, boolean ativo, Funcionario funcionario) {
        this(id, nome, login, email, senha, perfil, ativo);
        this.funcionario = funcionario;
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

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public FuncaoFuncionario getPerfil() {
        return perfil;
    }

    public void setPerfil(FuncaoFuncionario perfil) {
        this.perfil = perfil;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public Funcionario getFuncionario() {
        return funcionario;
    }

    public void setFuncionario(Funcionario funcionario) {
        this.funcionario = funcionario;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(login, usuario.login);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login);
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", login='" + login + '\'' +
                ", email='" + email + '\'' +
                ", perfil=" + perfil +
                ", ativo=" + ativo +
                '}';
    }
}
