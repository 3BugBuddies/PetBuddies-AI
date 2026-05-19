package br.com.fiap.petbuddies.dto.motor;

import br.com.fiap.petbuddies.domain.entity.EventoPlanoEntity;
import java.time.LocalDate;

public class EventoPlanoDto {

    private Long id;
    private String tipo;
    private String nome;
    private LocalDate dataAlvo;
    private String status;

    public static EventoPlanoDto from(EventoPlanoEntity e) {
        EventoPlanoDto dto = new EventoPlanoDto();
        dto.id = e.getId();
        dto.tipo = e.getTipo().name();
        dto.nome = e.getNome();
        dto.dataAlvo = e.getDataAlvo();
        dto.status = e.getStatus().name();
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public LocalDate getDataAlvo() { return dataAlvo; }
    public void setDataAlvo(LocalDate dataAlvo) { this.dataAlvo = dataAlvo; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
