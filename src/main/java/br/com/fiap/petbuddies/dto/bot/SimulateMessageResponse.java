package br.com.fiap.petbuddies.dto.bot;

public class SimulateMessageResponse {

    private String telefone;
    private String mensagemEnviada;
    private String respostaLLM;

    public SimulateMessageResponse(String telefone, String mensagemEnviada, String respostaLLM) {
        this.telefone = telefone;
        this.mensagemEnviada = mensagemEnviada;
        this.respostaLLM = respostaLLM;
    }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getMensagemEnviada() { return mensagemEnviada; }
    public void setMensagemEnviada(String v) { this.mensagemEnviada = v; }

    public String getRespostaLLM() { return respostaLLM; }
    public void setRespostaLLM(String v) { this.respostaLLM = v; }
}
