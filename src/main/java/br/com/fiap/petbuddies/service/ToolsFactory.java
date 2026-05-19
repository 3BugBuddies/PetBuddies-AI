package br.com.fiap.petbuddies.service;

import br.com.fiap.petbuddies.domain.enums.Intencao;
import br.com.fiap.petbuddies.tools.AgendamentoTools;
import br.com.fiap.petbuddies.tools.CadastroTools;
import br.com.fiap.petbuddies.tools.PlanoTools;
import br.com.fiap.petbuddies.tools.TriagemTools;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ToolsFactory {

    private final CadastroTools cadastroTools;
    private final AgendamentoTools agendamentoTools;
    private final PlanoTools planoTools;
    private final TriagemTools triagemTools;

    public ToolsFactory(CadastroTools cadastroTools, AgendamentoTools agendamentoTools,
                        PlanoTools planoTools, TriagemTools triagemTools) {
        this.cadastroTools = cadastroTools;
        this.agendamentoTools = agendamentoTools;
        this.planoTools = planoTools;
        this.triagemTools = triagemTools;
    }

    public List<Object> get(Intencao intencao) {
        return switch (intencao) {
            case CADASTRO       -> List.of(cadastroTools);
            case AGENDAMENTO    -> List.of(cadastroTools, agendamentoTools);
            case CONSULTA_PLANO -> List.of(cadastroTools, planoTools);
            case TRIAGEM        -> List.of(cadastroTools, triagemTools);
            case GERAL          -> List.of();
        };
    }
}
