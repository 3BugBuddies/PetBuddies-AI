package br.com.fiap.petbuddies.dto.prescricao;

import java.math.BigDecimal;
import java.util.List;

/**
 * Saída bruta do modelo para a narrativa do veterinário — o alvo do
 * {@code .entity()} do Spring AI. Cada campo de valor vem acompanhado de
 * {@code trecho} (a citação da narrativa que sustenta o valor) e
 * {@code literal} (dito diretamente vs. inferido/calculado pelo modelo).
 * Sem validação nenhuma aqui — {@link PrescricaoExtracaoService} decide o
 * que dela vira {@code PrescricaoRequest}.
 */
public record PrescricaoExtracaoIA(
        String medicamento,
        String trechoMedicamento,
        Boolean literalMedicamento,

        BigDecimal doseMin,
        String trechoDoseMin,
        Boolean literalDoseMin,

        BigDecimal doseMax,
        String trechoDoseMax,
        Boolean literalDoseMax,

        String unidade,
        String trechoUnidade,
        Boolean literalUnidade,

        Integer frequenciaDia,
        String trechoFrequenciaDia,
        Boolean literalFrequenciaDia,

        Integer duracaoDias,
        String trechoDuracaoDias,
        Boolean literalDuracaoDias,

        String dataInicio,
        String trechoDataInicio,
        Boolean literalDataInicio,

        String orientacao,
        String trechoOrientacao,
        Boolean literalOrientacao,

        List<RegraCondicionalExtraidaIA> regras) {
}
