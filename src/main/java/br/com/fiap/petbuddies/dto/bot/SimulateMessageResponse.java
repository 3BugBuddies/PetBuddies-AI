package br.com.fiap.petbuddies.dto.bot;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta do bot à mensagem simulada")
public class SimulateMessageResponse {

    @Schema(description = "Telefone do usuário")
    private String telefone;

    @Schema(description = "Mensagem enviada pelo usuário")
    private String mensagemEnviada;

    @Schema(description = "Resposta gerada pelo LLM")
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
