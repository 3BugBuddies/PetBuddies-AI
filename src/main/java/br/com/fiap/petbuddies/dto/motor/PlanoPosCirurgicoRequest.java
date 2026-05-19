package br.com.fiap.petbuddies.dto.motor;

import br.com.fiap.petbuddies.domain.enums.Especie;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

public class PlanoPosCirurgicoRequest {

    @NotNull @Positive
    private Long petNetApiAnimalId;

    @NotNull @Positive
    private Long petNetApiConsultaId;

    @NotNull
    private Especie especie;

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
