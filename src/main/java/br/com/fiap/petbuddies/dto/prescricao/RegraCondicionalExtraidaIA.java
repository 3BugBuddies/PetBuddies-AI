package br.com.fiap.petbuddies.dto.prescricao;

import java.math.BigDecimal;

/**
 * Uma regra condicional como o modelo a devolveu, antes de validação. {@code condicao} casa com um
 * código do catálogo da clínica; fora do catálogo, vem nulo e a condição vai em {@code condicaoNovaRotulo}
 * e {@code condicaoNovaTipoDado}.
 */
public record RegraCondicionalExtraidaIA(
        String condicao,
        String condicaoNovaRotulo,
        String condicaoNovaTipoDado,
        String operador,
        BigDecimal limite,
        String acaoDose,
        String trecho) {
}
