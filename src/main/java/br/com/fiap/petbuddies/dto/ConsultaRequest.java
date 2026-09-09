package br.com.fiap.petbuddies.dto;

import br.com.fiap.petbuddies.domain.enums.StatusConsulta;
import br.com.fiap.petbuddies.domain.enums.TipoConsulta;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

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

    public TipoConsulta getTipo() { return tipo; }
    public void setTipo(TipoConsulta tipo) { this.tipo = tipo; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public StatusConsulta getStatus() { return status; }
    public void setStatus(StatusConsulta status) { this.status = status; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public Long getAnimalId() { return animalId; }
    public void setAnimalId(Long animalId) { this.animalId = animalId; }

    public Long getVeterinarioId() { return veterinarioId; }
    public void setVeterinarioId(Long veterinarioId) { this.veterinarioId = veterinarioId; }
}
