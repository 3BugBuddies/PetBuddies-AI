package br.com.fiap.petbuddies.dto.motor;

import br.com.fiap.petbuddies.domain.enums.Especie;
import br.com.fiap.petbuddies.domain.enums.Porte;
import br.com.fiap.petbuddies.domain.enums.Sexo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

@Schema(description = "Dados do animal para instanciar plano preventivo")
public class PlanoPreventivoRequest {

    @Schema(description = "ID do animal no PetBuddies-API (.NET)", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull @Positive
    private Long petNetApiAnimalId;

    @Schema(description = "Espécie do animal", example = "CACHORRO", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Especie especie;

    @Schema(description = "Porte do animal", example = "MEDIO")
    private Porte porte;

    @Schema(description = "Sexo do animal", example = "MACHO")
    private Sexo sexo;

    @Schema(description = "Se o animal é castrado")
    private Boolean castrado;

    @Schema(description = "Data de nascimento do animal (deve ser no passado)", example = "2020-05-10", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull @Past
    private LocalDate dataNascimento;

    public Long getPetNetApiAnimalId() { return petNetApiAnimalId; }
    public void setPetNetApiAnimalId(Long petNetApiAnimalId) { this.petNetApiAnimalId = petNetApiAnimalId; }

    public Especie getEspecie() { return especie; }
    public void setEspecie(Especie especie) { this.especie = especie; }

    public Porte getPorte() { return porte; }
    public void setPorte(Porte porte) { this.porte = porte; }

    public Sexo getSexo() { return sexo; }
    public void setSexo(Sexo sexo) { this.sexo = sexo; }

    public Boolean getCastrado() { return castrado; }
    public void setCastrado(Boolean castrado) { this.castrado = castrado; }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
}
