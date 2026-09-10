package br.com.fiap.petbuddies.dto;

import br.com.fiap.petbuddies.domain.entity.PlanoCuidadoEntity;
import br.com.fiap.petbuddies.domain.enums.StatusItem;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Schema(description = "Plano de cuidado do animal")
public class PlanoResponse {

    @Schema(description = "ID do plano")
    private Long id;

    @Schema(description = "ID do animal no PetBuddies-API (.NET)")
    private Long animalId;

    @Schema(description = "ID do protocolo aplicado no catálogo do serviço .NET. Nulo em plano de tratamento")
    private Long protocoloId;

    @Schema(description = "Categoria do plano (PREVENTIVO, POS_CIRURGICO, TRATAMENTO)")
    private String categoria;

    @Schema(description = "Status atual do plano (ATIVO, CANCELADO…)")
    private String status;

    @Schema(description = "Data/hora de instanciação do plano")
    private LocalDateTime instanciadoEm;

    @Schema(description = "Eventos do plano (excluindo cancelados)")
    private List<ItemPlanoCuidadoDto> eventos;

    @Schema(description = "true se criado nesta chamada, false se já existia. null nos GETs")
    private Boolean criado;

    @Schema(description = "Motivo quando criado=false (ex: SEM_PROTOCOLO_COMPATIVEL). null nos GETs")
    private String motivo;

    public static PlanoResponse from(PlanoCuidadoEntity plano, Boolean criado, String motivo) {
        PlanoResponse r = new PlanoResponse();
        r.id = plano.getId();
        r.animalId = plano.getAnimalId();
        // protocoloId e nulavel: plano formado so por itens de prescricao nao tem molde
        r.protocoloId = plano.getProtocoloId();
        r.categoria = plano.getCategoria().name();
        r.status = plano.getStatus().name();
        r.instanciadoEm = plano.getCreatedAt();
        r.eventos = plano.getItens().stream()
                .filter(e -> e.getStatus() != StatusItem.CANCELADO)
                .map(ItemPlanoCuidadoDto::from)
                .collect(Collectors.toList());
        r.criado = criado;
        r.motivo = motivo;
        return r;
    }

    public static PlanoResponse from(PlanoCuidadoEntity plano) {
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

    public Long getAnimalId() { return animalId; }
    public void setAnimalId(Long v) { this.animalId = v; }

    public Long getProtocoloId() { return protocoloId; }
    public void setProtocoloId(Long v) { this.protocoloId = v; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getInstanciadoEm() { return instanciadoEm; }
    public void setInstanciadoEm(LocalDateTime v) { this.instanciadoEm = v; }


    public List<ItemPlanoCuidadoDto> getEventos() { return eventos; }
    public void setEventos(List<ItemPlanoCuidadoDto> eventos) { this.eventos = eventos; }

    public Boolean getCriado() { return criado; }
    public void setCriado(Boolean criado) { this.criado = criado; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
