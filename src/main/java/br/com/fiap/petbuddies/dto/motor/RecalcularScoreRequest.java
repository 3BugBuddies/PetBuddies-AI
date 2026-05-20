package br.com.fiap.petbuddies.dto.motor;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Dados para recalcular o score de risco do animal")
public class RecalcularScoreRequest {

    @Schema(description = "ID do animal no PetBuddies-API (.NET)", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull @Positive
    private Long petNetApiAnimalId;

    @Schema(description = "ID da consulta de referência (opcional)", example = "5")
    private Long petNetApiConsultaId;

    @Schema(description = "Motivo do recálculo", example = "POS_CONSULTA")
    private String motivo;

    public Long getPetNetApiAnimalId() { return petNetApiAnimalId; }
    public void setPetNetApiAnimalId(Long petNetApiAnimalId) { this.petNetApiAnimalId = petNetApiAnimalId; }

    public Long getPetNetApiConsultaId() { return petNetApiConsultaId; }
    public void setPetNetApiConsultaId(Long petNetApiConsultaId) { this.petNetApiConsultaId = petNetApiConsultaId; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
