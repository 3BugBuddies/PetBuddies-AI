package br.com.fiap.petbuddies.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class TriagemTools {

    @Tool(description = "Inicia uma sessão de triagem para o animal com o sintoma principal descrito")
    public String iniciarTriagem(Long animalId, String sintomaPrincipal) {
        return "STUB|iniciarTriagem não implementado ainda (PRD 08)";
    }

    @Tool(description = "Classifica a triagem com base nas respostas coletadas e retorna 🟢🟡🔴")
    public String classificarTriagem(Long sessaoId) {
        return "STUB|classificarTriagem não implementado ainda (PRD 08)";
    }

    @Tool(description = "Alerta a clínica em caso de emergência classificada como 🔴")
    public String alertarClinica(Long sessaoId) {
        return "STUB|alertarClinica não implementado ainda (PRD 08)";
    }
}
