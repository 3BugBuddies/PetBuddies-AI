package br.com.fiap.petbuddies.dto.prescricao;

import java.math.BigDecimal;

/** Regra como o modelo a devolveu, antes de validação; fora do catálogo, {@code condicao} vem nulo e vale a condição nova. */
public record RegraCondicionalExtraidaIA(
        String condicao,
        String condicaoNovaRotulo,
        String condicaoNovaTipoDado,
        String operador,
        BigDecimal limite,
        String acaoDose,
        String trecho) {
}
