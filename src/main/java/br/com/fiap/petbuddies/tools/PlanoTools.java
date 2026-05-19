package br.com.fiap.petbuddies.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class PlanoTools {

    @Tool(description = "Consulta o plano de cuidados preventivo ativo do animal")
    public String consultarPlanoDoAnimal(Long petNetApiAnimalId) {
        return "STUB|consultarPlanoDoAnimal não implementado ainda (PRD 06)";
    }

    @Tool(description = "Consulta o score de risco atual do animal com classificação BAIXO/MEDIO/ALTO")
    public String consultarScoreDoAnimal(Long petNetApiAnimalId) {
        return "STUB|consultarScoreDoAnimal não implementado ainda (PRD 06)";
    }
}
