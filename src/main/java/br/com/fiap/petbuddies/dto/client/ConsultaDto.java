package br.com.fiap.petbuddies.dto.client;

import java.time.LocalDateTime;

public class ConsultaDto {

    private Long id;
    private String tipoConsulta;
    private LocalDateTime dataHora;
    private String status;
    private Long animalId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTipoConsulta() { return tipoConsulta; }
    public void setTipoConsulta(String tipoConsulta) { this.tipoConsulta = tipoConsulta; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getAnimalId() { return animalId; }
    public void setAnimalId(Long animalId) { this.animalId = animalId; }
}
