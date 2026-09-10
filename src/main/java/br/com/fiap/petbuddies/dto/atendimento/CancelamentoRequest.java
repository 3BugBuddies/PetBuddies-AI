package br.com.fiap.petbuddies.dto.atendimento;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CancelamentoRequest {

    @NotBlank(message = "Motivo do cancelamento é obrigatório.")
    private String motivo;
}
