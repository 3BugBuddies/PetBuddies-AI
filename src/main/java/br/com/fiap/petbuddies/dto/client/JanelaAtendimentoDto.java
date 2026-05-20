package br.com.fiap.petbuddies.dto.client;

import java.time.LocalDateTime;

public class JanelaAtendimentoDto {

    private Long id;
    private LocalDateTime dataHoraInicio;
    private LocalDateTime dataHoraFim;
    private Integer duracaoSlot;
    private Integer veterinarioId;
    private String veterinarioNome;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getDataHoraInicio() { return dataHoraInicio; }
    public void setDataHoraInicio(LocalDateTime dataHoraInicio) { this.dataHoraInicio = dataHoraInicio; }
    public LocalDateTime getDataHoraFim() { return dataHoraFim; }
    public void setDataHoraFim(LocalDateTime dataHoraFim) { this.dataHoraFim = dataHoraFim; }
    public Integer getDuracaoSlot() { return duracaoSlot; }
    public void setDuracaoSlot(Integer duracaoSlot) { this.duracaoSlot = duracaoSlot; }
    public Integer getVeterinarioId() { return veterinarioId; }
    public void setVeterinarioId(Integer veterinarioId) { this.veterinarioId = veterinarioId; }
    public String getVeterinarioNome() { return veterinarioNome; }
    public void setVeterinarioNome(String veterinarioNome) { this.veterinarioNome = veterinarioNome; }
}
