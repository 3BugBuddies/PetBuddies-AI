package br.com.fiap.petbuddies.dto;

import br.com.fiap.petbuddies.domain.enums.Especie;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

@Schema(description = "Dados do animal para instanciar plano preventivo")
public class PlanoPreventivoRequest {

    @Schema(description = "ID do animal no PetBuddies-API (.NET)", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull @Positive
    private Long animalId;

    @Schema(description = "Espécie do animal", example = "CACHORRO", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Especie especie;

    @Schema(description = "Data de nascimento do animal (deve ser no passado)", example = "2020-05-10", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull @Past
    private LocalDate dataNascimento;

    public Long getAnimalId() { return animalId; }
    public void setAnimalId(Long animalId) { this.animalId = animalId; }

    public Especie getEspecie() { return especie; }
    public void setEspecie(Especie especie) { this.especie = especie; }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
}
