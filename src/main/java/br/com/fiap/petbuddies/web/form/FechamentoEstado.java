package br.com.fiap.petbuddies.web.form;

import java.util.List;
import java.util.Map;

/**
 * O que o Alpine precisa para montar as etapas. Só texto, número e booleano: nada de data,
 * porque o serializador de JavaScript do Thymeleaf não conhece java.time.
 */
public record FechamentoEstado(
        int etapa,
        boolean houveProcedimento,
        boolean prescrever,
        String tipo,
        String itemPlanoId,
        String nome,
        List<ItemAberto> itensAbertos,
        List<CondicaoOpcao> condicoes,
        List<RegraEstado> regras,
        Map<String, String> tipos,
        Map<String, String> operadores,
        Map<String, String> acoes,
        String receitaModeloId,
        String receitaModeloVersao,
        List<ReceitaOpcao> receitasModelo) {

    public record ItemAberto(String id, String nome, String tipo, String data, boolean vencido) {
    }

    /** {@code tipo} é BOOLEANO ou NUMERICO. */
    public record CondicaoOpcao(String id, String rotulo, String tipo, String unidade, boolean alarme) {
    }

    /** Receita-modelo ativa da clínica, com os números já em texto para preencher os campos como vieram. */
    public record ReceitaOpcao(String id, String versao, String titulo, String medicamento, String doseMin,
                               String doseMax, String unidade, String frequenciaDia, String duracaoDias,
                               String orientacao, List<RegraEstado> regras) {
    }

    public record RegraEstado(String condicaoClinicaId, String operador, String limite, String acaoDose) {
    }
}
