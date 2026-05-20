package br.com.fiap.petbuddies.dto.client;

public class CancelarConsultaRequest {

    private final String status = "CANCELADA";
    private String motivo;

    public CancelarConsultaRequest(String motivo) {
        this.motivo = motivo;
    }

    public String getStatus() { return status; }
    public String getMotivo() { return motivo; }
}
