package br.com.fiap.petbuddies.dto.identidade;

import br.com.fiap.petbuddies.domain.enums.identidade.TipoRegistro;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistroRequest {

    @NotNull(message = "Tipo é obrigatório.")
    @Schema(example = "TUTOR")
    private TipoRegistro tipo;

    @NotBlank(message = "Nome é obrigatório.")
    @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres.")
    private String nome;

    @NotBlank(message = "E-mail é obrigatório.")
    @Email(message = "E-mail inválido.")
    @Size(max = 120, message = "E-mail deve ter no máximo 120 caracteres.")
    @Schema(description = "Vira o login da conta.", example = "joana@email.com")
    private String email;

    @NotBlank(message = "Senha é obrigatória.")
    @Size(min = 6, max = 72, message = "Senha deve ter entre 6 e 72 caracteres.")
    private String senha;

    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres.")
    @Schema(description = "Obrigatório quando o tipo é TUTOR.", example = "11955550001")
    private String telefone;

    @Size(max = 30, message = "CRMV deve ter no máximo 30 caracteres.")
    @Schema(description = "Obrigatório quando o tipo é VET.", example = "SP-54321")
    private String crmv;

    @Override
    public String toString() {
        return "RegistroRequest{tipo=" + tipo + ", email='" + email + "'}";
    }
}
