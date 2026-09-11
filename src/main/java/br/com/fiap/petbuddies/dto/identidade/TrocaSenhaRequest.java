package br.com.fiap.petbuddies.dto.identidade;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrocaSenhaRequest {

    @NotBlank(message = "Senha atual é obrigatória.")
    private String senhaAtual;

    @NotBlank(message = "Senha nova é obrigatória.")
    @Size(min = 6, max = 72, message = "Senha deve ter entre 6 e 72 caracteres.")
    private String senhaNova;

    @Override
    public String toString() {
        return "TrocaSenhaRequest{}";
    }
}
