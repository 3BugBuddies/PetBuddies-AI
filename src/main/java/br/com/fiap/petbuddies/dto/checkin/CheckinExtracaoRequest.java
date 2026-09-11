package br.com.fiap.petbuddies.dto.checkin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

// So interpreta a narrativa — nao grava nada; a confirmacao e o passo seguinte, CheckinRequest.
// Sem itemPlanoCuidadoId: a extracao roda contra o vocabulario inteiro; o avaliador, no passo 2, decide contra qual prescricao a condicao conta.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CheckinExtracaoRequest {

    @NotNull(message = "Animal é obrigatório.")
    private Long animalId;

    /** Ausente, assume hoje. */
    private LocalDate dataReferencia;

    @NotBlank(message = "Narrativa é obrigatória.")
    @Size(max = 4000, message = "Narrativa deve ter no máximo 4000 caracteres.")
    private String narrativa;
}
