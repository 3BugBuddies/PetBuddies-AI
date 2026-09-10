package br.com.fiap.petbuddies.dto.identidade;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Login é obrigatório.")
    @Schema(example = "ana@clinica.com")
    private String login;

    @NotBlank(message = "Senha é obrigatória.")
    private String senha;

    /**
     * Sem a senha, de proposito: {@code toString} de DTO acaba em log de erro e
     * em mensagem de excecao, e a senha em texto claro nao pode chegar la.
     */
    @Override
    public String toString() {
        return "LoginRequest{login='" + login + "'}";
    }
}
