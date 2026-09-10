package br.com.fiap.petbuddies.dto.prescricao;

import java.math.BigDecimal;

/** Uma regra condicional como o modelo a devolveu, antes de validação — {@code condicao} precisa casar com um código do catálogo da clínica. */
public record RegraCondicionalExtraidaIA(
        String condicao,
        String operador,
        BigDecimal limite,
        String acaoDose,
        String trecho) {
}
