package br.com.fiap.petbuddies.dto.client;

public class AgendarConsultaRequest {

    private Long animalId;
    private Long janelaId;
    private String tipoConsulta;

    public AgendarConsultaRequest(Long animalId, Long janelaId, String tipoConsulta) {
        this.animalId = animalId;
        this.janelaId = janelaId;
        this.tipoConsulta = tipoConsulta;
    }

    public Long getAnimalId() { return animalId; }
    public Long getJanelaId() { return janelaId; }
    public String getTipoConsulta() { return tipoConsulta; }
}
