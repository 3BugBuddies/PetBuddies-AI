package br.com.fiap.petbuddies.dto.motor;

import br.com.fiap.petbuddies.domain.entity.PlanoCuidadoAnimalEntity;
import br.com.fiap.petbuddies.domain.enums.StatusEventoPlano;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Schema(description = "Plano de cuidado do animal")
public class PlanoResponse {

    @Schema(description = "ID do plano")
    private Long id;

    @Schema(description = "ID do animal no PetBuddies-API (.NET)")
    private Long petNetApiAnimalId;

    @Schema(description = "Nome do protocolo aplicado")
    private String protocoloNome;

    @Schema(description = "Categoria do protocolo (PREVENTIVO, POS_CIRURGICO…)")
    private String categoria;

    @Schema(description = "Status atual do plano (ATIVO, CANCELADO…)")
    private String status;

    @Schema(description = "Data/hora de instanciação do plano")
    private LocalDateTime instanciadoEm;

    @Schema(description = "Score de risco atual do animal")
    private Integer scoreAtual;

    @Schema(description = "Eventos do plano (excluindo cancelados)")
    private List<EventoPlanoDto> eventos;

    @Schema(description = "true se criado nesta chamada, false se já existia. null nos GETs")
    private Boolean criado;

    @Schema(description = "Motivo quando criado=false (ex: SEM_PROTOCOLO_COMPATIVEL). null nos GETs")
    private String motivo;

    public static PlanoResponse from(PlanoCuidadoAnimalEntity plano, Boolean criado, String motivo) {
        PlanoResponse r = new PlanoResponse();
        r.id = plano.getId();
        r.petNetApiAnimalId = plano.getPetNetApiAnimalId();
        r.protocoloNome = plano.getProtocolo().getNome();
        r.categoria = plano.getProtocolo().getCategoria().name();
        r.status = plano.getStatus().name();
        r.instanciadoEm = plano.getCreatedAt();
        r.scoreAtual = plano.getScoreAtual();
        r.eventos = plano.getEventos().stream()
                .filter(e -> e.getStatus() != StatusEventoPlano.CANCELADO)
                .map(EventoPlanoDto::from)
                .collect(Collectors.toList());
        r.criado = criado;
        r.motivo = motivo;
        return r;
    }

    public static PlanoResponse from(PlanoCuidadoAnimalEntity plano) {
        return from(plano, null, null);
    }

    public static PlanoResponse semProtocolo() {
        PlanoResponse r = new PlanoResponse();
        r.criado = false;
        r.motivo = "SEM_PROTOCOLO_COMPATIVEL";
        return r;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPetNetApiAnimalId() { return petNetApiAnimalId; }
    public void setPetNetApiAnimalId(Long v) { this.petNetApiAnimalId = v; }

    public String getProtocoloNome() { return protocoloNome; }
    public void setProtocoloNome(String v) { this.protocoloNome = v; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getInstanciadoEm() { return instanciadoEm; }
    public void setInstanciadoEm(LocalDateTime v) { this.instanciadoEm = v; }

    public Integer getScoreAtual() { return scoreAtual; }
    public void setScoreAtual(Integer scoreAtual) { this.scoreAtual = scoreAtual; }

    public List<EventoPlanoDto> getEventos() { return eventos; }
    public void setEventos(List<EventoPlanoDto> eventos) { this.eventos = eventos; }

    public Boolean getCriado() { return criado; }
    public void setCriado(Boolean criado) { this.criado = criado; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
