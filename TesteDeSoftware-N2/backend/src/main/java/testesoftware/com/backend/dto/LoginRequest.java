package testesoftware.com.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Credenciais enviadas pela tela de Login do PDV.
 */
public record LoginRequest(
        @NotBlank(message = "O e-mail é obrigatório.") String email,
        @NotBlank(message = "A senha é obrigatória.") String senha) {
}
