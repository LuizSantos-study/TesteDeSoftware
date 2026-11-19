package testesoftware.com.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import testesoftware.com.backend.enums.FuncaoFuncionario;

/**
 * Dados da tela de Cadastro de Funcionário. O CRF é obrigatório apenas para FARMACEUTICO
 * (regra validada em FuncionarioService).
 */
public record CadastroFuncionarioRequest(
        @NotBlank(message = "O nome é obrigatório.") String nome,
        @NotBlank(message = "O e-mail é obrigatório.") @Email(message = "E-mail inválido.") String email,
        @NotBlank(message = "A senha é obrigatória.") @Size(min = 4, message = "A senha deve ter no mínimo 4 caracteres.") String senha,
        @NotNull(message = "O cargo é obrigatório.") FuncaoFuncionario cargo,
        String crf,
        String cpf) {
}
