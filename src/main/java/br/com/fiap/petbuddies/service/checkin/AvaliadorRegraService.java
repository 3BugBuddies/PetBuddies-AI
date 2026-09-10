package br.com.fiap.petbuddies.service.checkin;

import br.com.fiap.petbuddies.domain.entity.CondicaoObservadaEntity;
import br.com.fiap.petbuddies.domain.entity.PrescricaoEntity;
import br.com.fiap.petbuddies.domain.entity.RegraPrescricaoEntity;
import br.com.fiap.petbuddies.domain.enums.prescricao.TipoDado;
import br.com.fiap.petbuddies.domain.enums.checkin.TipoDesfecho;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * O motor de regras do check-in (componente B do documento de IA): compara o
 * valor observado com a regra congelada pelo operador congelado e produz a
 * ação. Puro e sem repositório — só enxerga o que {@link CheckinService} já
 * carregou e gravou na transação; nunca lê {@code CondicaoClinicaEntity} para
 * decidir (ADR s3-10: quem avalia usa a cópia congelada, nunca relê o
 * catálogo).
 */
@Service
public class AvaliadorRegraService {

    public record Resultado(TipoDesfecho desfecho, BigDecimal doseAplicada, Long regraAplicadaId) {}

    /**
     * @param escalarPorCritica guardrail 6 (ADR s3-16): condição crítica
     *                          observada escala independente de regra de
     *                          prescrição, avaliada antes de qualquer regra.
     */
    public Resultado avaliar(
            PrescricaoEntity prescricao, List<RegraPrescricaoEntity> regras,
            List<CondicaoObservadaEntity> observadas, boolean escalarPorCritica) {

        if (escalarPorCritica) {
            return new Resultado(TipoDesfecho.ACIONAR_CLINICA, null, null);
        }

        for (RegraPrescricaoEntity regra : regras) { // já ordenadas por NR_ORDEM
            CondicaoObservadaEntity observada = encontrarObservada(regra, observadas);
            // Não observada != observada como falsa: regra só casa com o que o
            // tutor efetivamente confirmou naquele check-in.
            if (observada == null || !casa(regra, observada)) {
                continue;
            }
            return aplicarAcao(prescricao, regra);
        }

        return new Resultado(TipoDesfecho.DOSE_CALCULADA, doseBase(prescricao), null);
    }

    private CondicaoObservadaEntity encontrarObservada(RegraPrescricaoEntity regra, List<CondicaoObservadaEntity> observadas) {
        return observadas.stream()
                .filter(o -> o.getCondicaoClinica().getId().equals(regra.getCondicaoClinica().getId()))
                .findFirst()
                .orElse(null);
    }

    private boolean casa(RegraPrescricaoEntity regra, CondicaoObservadaEntity observada) {
        if (regra.getTipoDadoCongelado() == TipoDado.BOOLEANO) {
            // CK_REGRA_COERENCIA: regra BOOLEANO não tem operador nem limite —
            // a própria condição confirmada como verdadeira é o gatilho.
            return Boolean.TRUE.equals(observada.getValorBooleano());
        }
        if (observada.getValorNumerico() == null) {
            return false;
        }
        int cmp = observada.getValorNumerico().compareTo(regra.getLimite());
        return switch (regra.getOperador()) {
            case MAIOR_QUE -> cmp > 0;
            case MAIOR_OU_IGUAL -> cmp >= 0;
            case MENOR_QUE -> cmp < 0;
            case MENOR_OU_IGUAL -> cmp <= 0;
            case IGUAL -> cmp == 0;
        };
    }

    private Resultado aplicarAcao(PrescricaoEntity prescricao, RegraPrescricaoEntity regra) {
        return switch (regra.getAcaoDose()) {
            case DOSE_MIN -> new Resultado(TipoDesfecho.DOSE_CALCULADA, prescricao.getDoseMin(), regra.getId());
            case DOSE_MAX -> new Resultado(TipoDesfecho.DOSE_CALCULADA, prescricao.getDoseMax(), regra.getId());
            case DOSE_PADRAO -> new Resultado(TipoDesfecho.DOSE_CALCULADA, doseBase(prescricao), regra.getId());
            case ACIONAR_CLINICA -> new Resultado(TipoDesfecho.ACIONAR_CLINICA, null, regra.getId());
            // A prescrição não tem estado "pausada"; SEM_DOSE é o desfecho do
            // CK_ITEM_DESFECHO_DOSE que não exige NR_DOSE_APLICADA. Decisão de
            // implementação sem ADR — ver corpo do PR.
            case SUSPENDER -> new Resultado(TipoDesfecho.SEM_DOSE, null, regra.getId());
        };
    }

    /**
     * Base numérica de DOSE_PADRAO e do "nenhuma regra casou": a prescrição
     * não tem coluna de dose padrão, só a faixa NR_DOSE_MIN/NR_DOSE_MAX.
     * NR_DOSE_MIN — o piso da faixa, mais conservador — por decisão de
     * implementação sem ADR, marcada como pendente no corpo do PR.
     */
    private BigDecimal doseBase(PrescricaoEntity prescricao) {
        return prescricao.getDoseMin();
    }
}
