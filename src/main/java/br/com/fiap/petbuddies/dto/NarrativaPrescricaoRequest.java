package br.com.fiap.petbuddies.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NarrativaPrescricaoRequest {

    @NotNull(message = "Registro de atendimento é obrigatório.")
    private Long registroAtendimentoId;

    @NotBlank(message = "Narrativa é obrigatória.")
    @Size(max = 4000, message = "Narrativa deve ter no máximo 4000 caracteres.")
    private String narrativa;
}
