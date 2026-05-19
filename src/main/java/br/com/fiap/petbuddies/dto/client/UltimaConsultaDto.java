package br.com.fiap.petbuddies.dto.client;

import java.time.LocalDateTime;

public class UltimaConsultaDto {

    private LocalDateTime dataHora;
    private String tipo;

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}
