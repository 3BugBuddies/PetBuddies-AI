package br.com.fiap.petbuddies.dto.bot;

import br.com.fiap.petbuddies.domain.enums.Intencao;

public class ConversationContext {

    private String telefone;
    private Intencao intencaoAtual;
    private boolean responsavelIdentificado;
    private Long responsavelId;
    private String responsavelNome;
    private String stageAtual;
    private String dadosColetados;

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

    public String getResponsavelNome() { return responsavelNome; }
    public void setResponsavelNome(String responsavelNome) { this.responsavelNome = responsavelNome; }

    public String getStageAtual() { return stageAtual; }
    public void setStageAtual(String stageAtual) { this.stageAtual = stageAtual; }

    public String getDadosColetados() { return dadosColetados; }
    public void setDadosColetados(String dadosColetados) { this.dadosColetados = dadosColetados; }
}
