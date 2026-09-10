package br.com.fiap.petbuddies.dto;

import br.com.fiap.petbuddies.domain.enums.StatusConsulta;
import br.com.fiap.petbuddies.domain.enums.TipoConsulta;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaRequest {

    @NotNull(message = "Tipo da consulta é obrigatório.")
    private TipoConsulta tipo;

    @NotNull(message = "Data e hora são obrigatórias.")
    private LocalDateTime dataHora;

    /** Ausente na criação, a consulta nasce AGENDADA. */
    private StatusConsulta status;

    @Size(max = 2000, message = "Observação deve ter no máximo 2000 caracteres.")
    private String observacao;

    @Size(max = 2000, message = "Motivo deve ter no máximo 2000 caracteres.")
    private String motivo;

    @NotNull(message = "Animal é obrigatório.")
    private Long animalId;

    @NotNull(message = "Veterinário é obrigatório.")
    private Long veterinarioId;
}
