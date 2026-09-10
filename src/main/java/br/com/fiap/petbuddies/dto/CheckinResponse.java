package br.com.fiap.petbuddies.dto;

import br.com.fiap.petbuddies.domain.entity.CheckinEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class CheckinResponse {

    private Long id;
    private Long animalId;
    private Long itemPlanoCuidadoId;
    private LocalDate dataReferencia;
    private LocalDateTime registradoEm;
    private String narrativa;
    private String observacoesGerais;
    private String ticUtilizada;
    private List<CondicaoObservadaResponse> condicoesObservadas;
    private List<CheckinDesfechoResponse> desfechos;
    private boolean escalado;

    public static CheckinResponse of(
            CheckinEntity checkin, List<CondicaoObservadaResponse> condicoesObservadas,
            List<CheckinDesfechoResponse> desfechos, boolean escalado) {
        CheckinResponse dto = new CheckinResponse();
        dto.id = checkin.getId();
        dto.animalId = checkin.getAnimal() == null ? null : checkin.getAnimal().getId();
        dto.itemPlanoCuidadoId = checkin.getItemPlanoCuidado() == null ? null : checkin.getItemPlanoCuidado().getId();
        dto.dataReferencia = checkin.getDataReferencia();
        dto.registradoEm = checkin.getRegistradoEm();
        dto.narrativa = checkin.getNarrativa();
        dto.observacoesGerais = checkin.getObservacoesGerais();
        dto.ticUtilizada = checkin.getTicUtilizada();
        dto.condicoesObservadas = condicoesObservadas;
        dto.desfechos = desfechos;
        dto.escalado = escalado;
        return dto;
    }

    public Long getId() { return id; }
    public Long getAnimalId() { return animalId; }
    public Long getItemPlanoCuidadoId() { return itemPlanoCuidadoId; }
    public LocalDate getDataReferencia() { return dataReferencia; }
    public LocalDateTime getRegistradoEm() { return registradoEm; }
    public String getNarrativa() { return narrativa; }
    public String getObservacoesGerais() { return observacoesGerais; }
    public String getTicUtilizada() { return ticUtilizada; }
    public List<CondicaoObservadaResponse> getCondicoesObservadas() { return condicoesObservadas; }
    public List<CheckinDesfechoResponse> getDesfechos() { return desfechos; }
    public boolean isEscalado() { return escalado; }
}
