package br.com.fiap.petbuddies.dto;

import jakarta.validation.constraints.NotBlank;

public class CancelamentoRequest {

    @NotBlank(message = "Motivo do cancelamento é obrigatório.")
    private String motivo;

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
