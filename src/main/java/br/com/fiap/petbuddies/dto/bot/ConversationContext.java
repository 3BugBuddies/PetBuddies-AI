package br.com.fiap.petbuddies.dto.bot;

import br.com.fiap.petbuddies.domain.enums.Intencao;

public class ConversationContext {

    private String telefone;
    private Intencao intencaoAtual;
    private boolean responsavelIdentificado;
    private Long responsavelId;

    public ConversationContext() {}

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public Intencao getIntencaoAtual() { return intencaoAtual; }
    public void setIntencaoAtual(Intencao intencaoAtual) { this.intencaoAtual = intencaoAtual; }

    public boolean isResponsavelIdentificado() { return responsavelIdentificado; }
    public void setResponsavelIdentificado(boolean responsavelIdentificado) {
        this.responsavelIdentificado = responsavelIdentificado;
    }

    public Long getResponsavelId() { return responsavelId; }
    public void setResponsavelId(Long responsavelId) { this.responsavelId = responsavelId; }
}
