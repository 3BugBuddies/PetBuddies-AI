package br.com.fiap.petbuddies.dto.cuidado;

import br.com.fiap.petbuddies.domain.entity.AnimalEntity;
import br.com.fiap.petbuddies.domain.entity.ConsultaEntity;
import br.com.fiap.petbuddies.domain.enums.cadastro.Especie;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.*;
import java.time.LocalDateTime;

@Schema(description = "Dados para instanciar plano de recuperação pós-cirúrgica")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlanoPosCirurgicoRequest {

    @Schema(description = "ID do animal no PetBuddies-API (.NET)", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull @Positive
    private Long animalId;

    @Schema(description = "ID da consulta cirúrgica no PetBuddies-API (.NET)", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull @Positive
    private Long consultaId;

    @Schema(description = "Espécie do animal", example = "GATO", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Especie especie;

    @Schema(description = "Data/hora da realização da cirurgia", example = "2026-05-01T14:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull @PastOrPresent
    private LocalDateTime dataRealizacao;

    public static PlanoPosCirurgicoRequest from(AnimalEntity animal, ConsultaEntity consulta) {
        PlanoPosCirurgicoRequest request = new PlanoPosCirurgicoRequest();
        request.setAnimalId(animal.getId());
        request.setConsultaId(consulta.getId());
        request.setEspecie(animal.getEspecie());
        request.setDataRealizacao(consulta.getDataHora());
        return request;
    }
}
