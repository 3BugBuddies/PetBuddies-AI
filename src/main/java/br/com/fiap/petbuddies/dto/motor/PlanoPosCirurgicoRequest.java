package br.com.fiap.petbuddies.dto.motor;

import br.com.fiap.petbuddies.domain.enums.Especie;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

@Schema(description = "Dados para instanciar plano de recuperação pós-cirúrgica")
public class PlanoPosCirurgicoRequest {

    @Schema(description = "ID do animal no PetBuddies-API (.NET)", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull @Positive
    private Long petNetApiAnimalId;

    @Schema(description = "ID da consulta cirúrgica no PetBuddies-API (.NET)", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull @Positive
    private Long petNetApiConsultaId;

    @Schema(description = "Espécie do animal", example = "GATO", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Especie especie;

    @Schema(description = "Data/hora da realização da cirurgia", example = "2026-05-01T14:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull @PastOrPresent
    private LocalDateTime dataRealizacao;

    public Long getPetNetApiAnimalId() { return petNetApiAnimalId; }
    public void setPetNetApiAnimalId(Long petNetApiAnimalId) { this.petNetApiAnimalId = petNetApiAnimalId; }

    public Long getPetNetApiConsultaId() { return petNetApiConsultaId; }
    public void setPetNetApiConsultaId(Long petNetApiConsultaId) { this.petNetApiConsultaId = petNetApiConsultaId; }

    public Especie getEspecie() { return especie; }
    public void setEspecie(Especie especie) { this.especie = especie; }

    public LocalDateTime getDataRealizacao() { return dataRealizacao; }
    public void setDataRealizacao(LocalDateTime dataRealizacao) { this.dataRealizacao = dataRealizacao; }
}
