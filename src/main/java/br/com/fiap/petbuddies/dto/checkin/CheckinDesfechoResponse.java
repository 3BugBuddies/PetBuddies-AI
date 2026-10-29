package br.com.fiap.petbuddies.dto.checkin;

import br.com.fiap.petbuddies.domain.enums.checkin.TipoDesfecho;
import br.com.fiap.petbuddies.domain.entity.PrescricaoEntity;
import br.com.fiap.petbuddies.domain.entity.RegraPrescricaoEntity;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    private String regraDescricao;
    private String veterinarioNome;
    private String veterinarioCrmv;
    private String clinicaNome;
    private LocalDateTime prescritaEm;

    public static CheckinDesfechoResponse of(
            Long prescricaoId, String medicamento, Long itemPlanoCuidadoId,
            TipoDesfecho desfecho, BigDecimal doseAplicada, String unidade, Long regraAplicadaId,
            PrescricaoEntity prescricao) {
        CheckinDesfechoResponse dto = new CheckinDesfechoResponse();
        dto.prescricaoId = prescricaoId;
        dto.medicamento = medicamento;
        dto.itemPlanoCuidadoId = itemPlanoCuidadoId;
        dto.desfecho = desfecho;
        dto.doseAplicada = doseAplicada;
        dto.unidade = unidade;
        dto.regraAplicadaId = regraAplicadaId;
        if (prescricao != null) {
            dto.veterinarioNome = prescricao.getVeterinario().getNome();
            dto.veterinarioCrmv = prescricao.getVeterinario().getCrmv();
            dto.clinicaNome = prescricao.getVeterinario().getClinica().getNome();
            dto.prescritaEm = prescricao.getAuditoria().getCreatedAt();
        }
        return dto;
    }

    public CheckinDesfechoResponse comRegra(RegraPrescricaoEntity regra) {
        if (regra != null) {
            String acao = switch (regra.getAcaoDose()) {
                case DOSE_MIN -> "dose mínima";
                case DOSE_MAX -> "dose máxima";
                case DOSE_PADRAO -> "dose padrão";
                case ACIONAR_CLINICA -> "acionar clínica";
                case SUSPENDER -> "suspender";
            };
            this.regraDescricao = regra.getCondicaoCongelada().getRotulo() + " → " + acao;
        }
        return this;
    }
}
