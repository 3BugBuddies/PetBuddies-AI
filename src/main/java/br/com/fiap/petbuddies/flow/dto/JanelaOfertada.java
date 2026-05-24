package br.com.fiap.petbuddies.flow.dto;

import java.time.LocalDateTime;

public class JanelaOfertada {

    private Integer ordem;
    private Long janelaId;
    private LocalDateTime dataHoraInicio;
    private LocalDateTime dataHoraFim;
    private String veterinarioNome;

    public JanelaOfertada() {}

    public Integer getOrdem() { return ordem; }
    public void setOrdem(Integer ordem) { this.ordem = ordem; }

    public Long getJanelaId() { return janelaId; }
    public void setJanelaId(Long janelaId) { this.janelaId = janelaId; }

    public LocalDateTime getDataHoraInicio() { return dataHoraInicio; }
    public void setDataHoraInicio(LocalDateTime dataHoraInicio) { this.dataHoraInicio = dataHoraInicio; }

    public LocalDateTime getDataHoraFim() { return dataHoraFim; }
    public void setDataHoraFim(LocalDateTime dataHoraFim) { this.dataHoraFim = dataHoraFim; }

    public String getVeterinarioNome() { return veterinarioNome; }
    public void setVeterinarioNome(String veterinarioNome) { this.veterinarioNome = veterinarioNome; }
}
