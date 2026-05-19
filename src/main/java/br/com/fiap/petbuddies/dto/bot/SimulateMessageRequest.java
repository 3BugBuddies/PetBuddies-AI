package br.com.fiap.petbuddies.dto.bot;

import jakarta.validation.constraints.NotBlank;

public class SimulateMessageRequest {

    @NotBlank
    private String telefone;

    @NotBlank
    private String texto;

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }
}
