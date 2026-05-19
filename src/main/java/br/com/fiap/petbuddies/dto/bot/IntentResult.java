package br.com.fiap.petbuddies.dto.bot;

import br.com.fiap.petbuddies.domain.enums.Intencao;

public class IntentResult {

    private Intencao intencao;
    private Double confianca;

    public IntentResult() {}

    public IntentResult(Intencao intencao, Double confianca) {
        this.intencao = intencao;
        this.confianca = confianca;
    }

    public Intencao getIntencao() { return intencao; }
    public void setIntencao(Intencao intencao) { this.intencao = intencao; }

    public Double getConfianca() { return confianca; }
    public void setConfianca(Double confianca) { this.confianca = confianca; }
}
