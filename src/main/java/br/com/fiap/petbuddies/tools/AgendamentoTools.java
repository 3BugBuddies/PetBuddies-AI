package br.com.fiap.petbuddies.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class AgendamentoTools {

    @Tool(description = "Lista as janelas de atendimento disponíveis na clínica")
    public String listarJanelasDisponiveis() {
        return "STUB|listarJanelasDisponiveis não implementado ainda (PRD 06)";
    }

    @Tool(description = "Agenda uma consulta para o animal em uma janela de atendimento")
    public String agendarConsulta(Long animalId, Long janelaId, String tipoConsulta) {
        return "STUB|agendarConsulta não implementado ainda (PRD 06)";
    }

    @Tool(description = "Lista as consultas agendadas de um animal")
    public String listarConsultasDoAnimal(Long animalId) {
        return "STUB|listarConsultasDoAnimal não implementado ainda (PRD 06)";
    }

    @Tool(description = "Cancela uma consulta agendada informando o motivo")
    public String cancelarConsulta(Long consultaId, String motivo) {
        return "STUB|cancelarConsulta não implementado ainda (PRD 06)";
    }
}
