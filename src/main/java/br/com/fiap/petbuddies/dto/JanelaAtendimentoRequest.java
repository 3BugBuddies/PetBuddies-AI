package br.com.fiap.petbuddies.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JanelaAtendimentoRequest {

    @NotNull(message = "Data e hora de início são obrigatórias.")
    private LocalDateTime dataHoraInicio;

    @NotNull(message = "Veterinário é obrigatório.")
    private Long veterinarioId;

    /** Ausente, o slot nasce livre. Preenchido, reserva o horário para a consulta. */
    private Long consultaId;
}
