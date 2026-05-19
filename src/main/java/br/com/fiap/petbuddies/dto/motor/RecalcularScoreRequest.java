package br.com.fiap.petbuddies.dto.motor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class RecalcularScoreRequest {

    @NotNull @Positive
    private Long petNetApiAnimalId;

    private Long petNetApiConsultaId;
    private String motivo;

    public Long getPetNetApiAnimalId() { return petNetApiAnimalId; }
    public void setPetNetApiAnimalId(Long petNetApiAnimalId) { this.petNetApiAnimalId = petNetApiAnimalId; }

    public Long getPetNetApiConsultaId() { return petNetApiConsultaId; }
    public void setPetNetApiConsultaId(Long petNetApiConsultaId) { this.petNetApiConsultaId = petNetApiConsultaId; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
