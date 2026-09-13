package br.com.fiap.petbuddies.dto.atendimento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CancelamentoRequest {

    @NotBlank(message = "Motivo do cancelamento é obrigatório.")
    @Size(max = 2000, message = "Motivo deve ter no máximo 2000 caracteres.")
    private String motivo;
}
