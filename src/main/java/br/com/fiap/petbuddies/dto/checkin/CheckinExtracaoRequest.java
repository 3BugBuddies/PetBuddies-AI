package br.com.fiap.petbuddies.dto.checkin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

/**
 * Passo 1 do check-in (§5.1 do documento de IA): só interpreta a narrativa e
 * devolve o que foi entendido. Não grava nada — a confirmação do tutor é o
 * passo seguinte, {@link CheckinRequest}.
 *
 * <p>Sem {@code itemPlanoCuidadoId}: a extração sempre roda contra o
 * vocabulário inteiro em vigor para o animal, relato geral ou por item — é o
 * avaliador, no passo 2, que decide contra qual prescrição a condição
 * confirmada conta.</p>
 */
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
