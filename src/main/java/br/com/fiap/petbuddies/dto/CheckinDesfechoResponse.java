package br.com.fiap.petbuddies.dto;

import br.com.fiap.petbuddies.domain.enums.TipoDesfecho;

import java.math.BigDecimal;

/**
 * O que o motor decidiu para uma prescrição avaliada neste check-in.
 * {@code itemPlanoCuidadoId} nulo significa que não existe item do dia para
 * esta prescrição — a avaliação acontece e é devolvida aqui, mas não há onde
 * gravar {@code TP_DESFECHO} (ver "o que revisar com atenção" no corpo do PR).
 */
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

    public Long getPrescricaoId() { return prescricaoId; }
    public String getMedicamento() { return medicamento; }
    public Long getItemPlanoCuidadoId() { return itemPlanoCuidadoId; }
    public TipoDesfecho getDesfecho() { return desfecho; }
    public BigDecimal getDoseAplicada() { return doseAplicada; }
    public String getUnidade() { return unidade; }
    public Long getRegraAplicadaId() { return regraAplicadaId; }
}
