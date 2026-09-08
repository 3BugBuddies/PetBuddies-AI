package br.com.fiap.petbuddies.dto.motor;

import br.com.fiap.petbuddies.domain.entity.EventoPlanoEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "Evento de um plano de cuidado")
public class EventoPlanoDto {

    @Schema(description = "ID do evento")
    private Long id;

    @Schema(description = "Tipo do evento (VACINACAO, VERMIFUGACAO, EXAME, RETORNO, CIRURGIA, MEDICACAO, HIGIENE)")
    private String tipo;

    @Schema(description = "Nome descritivo do evento")
    private String nome;

    @Schema(description = "Data-alvo para realização do evento")
    private LocalDate dataAlvo;

    @Schema(description = "Status atual do evento (PENDENTE, REALIZADO, CANCELADO, ATRASADO)")
    private String status;

    @Schema(description = "Origem do item: PROTOCOLO (o molde do catálogo) ou PRESCRICAO (ato assinado)")
    private String origem;

    @Schema(description = "ID da prescrição no PetBuddies-API (.NET). Preenchido apenas quando origem = PRESCRICAO")
    private Long prescricaoId;

    public static EventoPlanoDto from(EventoPlanoEntity e) {
        EventoPlanoDto dto = new EventoPlanoDto();
        dto.id = e.getId();
        dto.tipo = e.getTipo().name();
        dto.nome = e.getNome();
        dto.dataAlvo = e.getDataAlvo();
        dto.status = e.getStatus().name();
        dto.origem = e.getOrigem() != null ? e.getOrigem().name() : null;
        dto.prescricaoId = e.getPrescricaoId();
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

    public String getOrigem() { return origem; }
    public void setOrigem(String origem) { this.origem = origem; }

    public Long getPrescricaoId() { return prescricaoId; }
    public void setPrescricaoId(Long prescricaoId) { this.prescricaoId = prescricaoId; }
}
