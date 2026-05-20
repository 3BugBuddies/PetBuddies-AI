package br.com.fiap.petbuddies.dto.bot;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Mensagem simulada para testar o bot sem Evolution API")
public class SimulateMessageRequest {

    @Schema(description = "Número do telefone do usuário (sem código de país)", example = "11999999999", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String telefone;

    @Schema(description = "Texto da mensagem a ser processada pelo bot", example = "quero cadastrar meu gato", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String texto;

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }
}
