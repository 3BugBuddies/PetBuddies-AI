package br.com.fiap.petbuddies.dto;

import br.com.fiap.petbuddies.domain.entity.CheckinEntity;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
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
}
