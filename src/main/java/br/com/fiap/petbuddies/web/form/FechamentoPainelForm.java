package br.com.fiap.petbuddies.web.form;

import br.com.fiap.petbuddies.domain.enums.atendimento.TipoProcedimento;
import br.com.fiap.petbuddies.dto.atendimento.FechamentoAtendimentoRequest;
import br.com.fiap.petbuddies.dto.atendimento.ProcedimentoFechamentoRequest;
import br.com.fiap.petbuddies.dto.atendimento.RegistroAtendimentoFechamentoRequest;
import br.com.fiap.petbuddies.dto.prescricao.PrescricaoFechamentoRequest;
import br.com.fiap.petbuddies.dto.prescricao.RegraPrescricaoFechamentoRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * O formulário das quatro etapas, achatado. Procedimento e prescrição são opcionais, então a validação
 * não pode ficar nas anotações do form: o controller monta o {@link FechamentoAtendimentoRequest}
 * só com o que foi escolhido e valida esse.
 */
@Getter
@Setter
@NoArgsConstructor
public class FechamentoPainelForm {

    private RegistroAtendimentoFechamentoRequest registro = new RegistroAtendimentoFechamentoRequest();
    private boolean houveProcedimento;
    private ProcedimentoPainel procedimento = new ProcedimentoPainel();
    private boolean prescrever;
    private PrescricaoFechamentoRequest prescricao = new PrescricaoFechamentoRequest();

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ProcedimentoPainel {
        private TipoProcedimento tipo;
        private String nome;
        private String lote;
        private LocalDateTime data;
        private String observacao;
        private Long itemPlanoId;
    }

    public FechamentoAtendimentoRequest paraRequest() {
        FechamentoAtendimentoRequest request = new FechamentoAtendimentoRequest();
        request.setRegistroAtendimento(registro);
        request.setProcedimentos(houveProcedimento ? List.of(procedimentoRequest()) : List.of());
        request.setPrescricoes(prescrever ? List.of(prescricaoRequest()) : List.of());
        return request;
    }

    private ProcedimentoFechamentoRequest procedimentoRequest() {
        ProcedimentoFechamentoRequest p = new ProcedimentoFechamentoRequest();
        p.setTipo(procedimento.tipo);
        p.setNome(procedimento.nome);
        p.setObservacao(observacaoComLote());
        // Procedimento feito é um fato: início e fim são o mesmo instante, como no contrato do fechamento.
        p.setDataPrevistaInicio(procedimento.data);
        p.setDataPrevistaFim(procedimento.data);
        boolean baixaItem = procedimento.tipo == TipoProcedimento.VACINACAO || procedimento.tipo == TipoProcedimento.VERMIFUGACAO;
        p.setItemPlanoId(baixaItem ? procedimento.itemPlanoId : null);
        return p;
    }

    // O lote não tem coluna própria: entra na observação, como no contrato do fechamento.
    private String observacaoComLote() {
        String lote = procedimento.lote == null ? "" : procedimento.lote.trim();
        String obs = procedimento.observacao == null ? "" : procedimento.observacao.trim();
        if (lote.isEmpty()) {
            return obs.isEmpty() ? null : obs;
        }
        return obs.isEmpty() ? "Lote " + lote : "Lote " + lote + ". " + obs;
    }

    private PrescricaoFechamentoRequest prescricaoRequest() {
        List<RegraPrescricaoFechamentoRequest> regras = prescricao.getRegras() == null
                ? new ArrayList<>() : prescricao.getRegras();
        for (int i = 0; i < regras.size(); i++) {
            regras.get(i).setOrdem(i + 1);
        }
        prescricao.setRegras(regras);
        return prescricao;
    }

    /** Caminho de violação do request → campo deste formulário; nulo quando não há campo equivalente. */
    public static String campoDoFormulario(String caminho) {
        String campo = caminho
                .replace("registroAtendimento.", "registro.")
                .replace("procedimentos[0].", "procedimento.")
                .replace("prescricoes[0].", "prescricao.");
        campo = campo.replace("procedimento.dataPrevistaInicio", "procedimento.data")
                .replace("procedimento.dataPrevistaFim", "procedimento.data");
        return campo.startsWith("registro.") || campo.startsWith("procedimento.") || campo.startsWith("prescricao.")
                ? campo : null;
    }

    /** Em que etapa mora o campo: 1 registro, 2 procedimento, 3 prescrição. */
    public static int etapaDoCampo(String campo) {
        if (campo == null) {
            return 4;
        }
        if (campo.startsWith("registro")) {
            return 1;
        }
        return campo.startsWith("procedimento") ? 2 : 3;
    }
}
