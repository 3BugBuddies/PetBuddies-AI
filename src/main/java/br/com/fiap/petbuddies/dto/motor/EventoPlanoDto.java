package br.com.fiap.petbuddies.dto.motor;

import br.com.fiap.petbuddies.domain.entity.EventoPlanoEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "Evento de um plano de cuidado")
public class EventoPlanoDto {

    @Schema(description = "ID do evento")
    private Long id;

    @Schema(description = "Tipo do evento (VACINA, CONSULTA_ROTINA, EXAME…)")
    private String tipo;

    @Schema(description = "Nome descritivo do evento")
    private String nome;

    @Schema(description = "Data-alvo para realização do evento")
    private LocalDate dataAlvo;

    @Schema(description = "Status atual do evento (PENDENTE, CONCLUIDO, CANCELADO)")
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
