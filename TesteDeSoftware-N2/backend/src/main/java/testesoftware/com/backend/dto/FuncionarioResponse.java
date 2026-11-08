package testesoftware.com.backend.dto;

import testesoftware.com.backend.enums.FuncaoFuncionario;
import testesoftware.com.backend.model.Funcionario;

/**
 * Funcionário retornado pela API (nunca expõe a senha).
 */
public record FuncionarioResponse(
        Long id, String nome, String email, String cpf, String crf, FuncaoFuncionario funcao) {

    public static FuncionarioResponse from(Funcionario f) {
        return f == null ? null : new FuncionarioResponse(
                f.getId(), f.getNome(), f.getEmail(), f.getCpf(), f.getCrf(), f.getFuncao());
    }
}
