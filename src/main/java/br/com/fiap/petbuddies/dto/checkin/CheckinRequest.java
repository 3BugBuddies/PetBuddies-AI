package br.com.fiap.petbuddies.dto.checkin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

// A linha so nasce aqui, depois que o tutor validou o que a IA entendeu. Nenhuma chamada ao modelo acontece neste passo.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CheckinRequest {

    @NotNull(message = "Animal é obrigatório.")
    private Long animalId;

    /** Ausente, assume hoje. */
    private LocalDate dataReferencia;

    @NotBlank(message = "Narrativa é obrigatória.")
    @Size(max = 4000, message = "Narrativa deve ter no máximo 4000 caracteres.")
    private String narrativa;

    /** Relato sobre um item específico do plano, em vez do relato geral do dia. */
    private Long itemPlanoCuidadoId;

    @Size(max = 120, message = "TIC utilizada deve ter no máximo 120 caracteres.")
    private String ticUtilizada;

    /** Vazia é um check-in válido: "nada a relatar hoje". */
    private List<@Valid CondicaoConfirmadaRequest> condicoes = List.of();
}
