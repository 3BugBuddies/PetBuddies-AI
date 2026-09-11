package br.com.fiap.petbuddies.dto.checkin;

import br.com.fiap.petbuddies.domain.enums.checkin.TipoDesfecho;
import lombok.*;

import java.math.BigDecimal;

// itemPlanoCuidadoId nulo: nao existe item do dia para esta prescricao — a avaliacao acontece e e devolvida, mas nao ha onde gravar TP_DESFECHO.
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CheckinDesfechoResponse {

    private Long prescricaoId;
    private String medicamento;
    private Long itemPlanoCuidadoId;
    private TipoDesfecho desfecho;
    private BigDecimal doseAplicada;
    private String unidade;
    private Long regraAplicadaId;

    public static CheckinDesfechoResponse of(
            Long prescricaoId, String medicamento, Long itemPlanoCuidadoId,
            TipoDesfecho desfecho, BigDecimal doseAplicada, String unidade, Long regraAplicadaId) {
        CheckinDesfechoResponse dto = new CheckinDesfechoResponse();
        dto.prescricaoId = prescricaoId;
        dto.medicamento = medicamento;
        dto.itemPlanoCuidadoId = itemPlanoCuidadoId;
        dto.desfecho = desfecho;
        dto.doseAplicada = doseAplicada;
        dto.unidade = unidade;
        dto.regraAplicadaId = regraAplicadaId;
        return dto;
    }
}
