package br.com.fiap.petbuddies.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class JanelaAtendimentoRequest {

    @NotNull(message = "Data e hora de início são obrigatórias.")
    private LocalDateTime dataHoraInicio;

    @NotNull(message = "Veterinário é obrigatório.")
    private Long veterinarioId;

    /** Ausente, o slot nasce livre. Preenchido, reserva o horário para a consulta. */
    private Long consultaId;

    public LocalDateTime getDataHoraInicio() { return dataHoraInicio; }
    public void setDataHoraInicio(LocalDateTime dataHoraInicio) { this.dataHoraInicio = dataHoraInicio; }

    public Long getVeterinarioId() { return veterinarioId; }
    public void setVeterinarioId(Long veterinarioId) { this.veterinarioId = veterinarioId; }

    public Long getConsultaId() { return consultaId; }
    public void setConsultaId(Long consultaId) { this.consultaId = consultaId; }
}
