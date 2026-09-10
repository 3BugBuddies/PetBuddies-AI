package br.com.fiap.petbuddies.dto;

import br.com.fiap.petbuddies.domain.entity.ItemPlanoCuidadoEntity;
import br.com.fiap.petbuddies.domain.entity.PlanoCuidadoEntity;
import br.com.fiap.petbuddies.domain.enums.StatusItem;
import br.com.fiap.petbuddies.domain.enums.TipoOrigemItem;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * O que um protocolo já produziu num plano: os itens nascidos do catálogo,
 * separados por realizados, pendentes e vencidos. Itens de origem PRESCRICAO
 * e CANCELADO ficam fora — não são o que o protocolo aplicou.
 */
@Schema(description = "Protocolo aplicado a um plano do animal, com os itens que ele produziu")
public class ProtocoloAplicadoResponse {

    @Schema(description = "ID do plano")
    private Long planoId;

    @Schema(description = "ID do animal no PetBuddies-API (.NET)")
    private Long animalId;

    @Schema(description = "ID do protocolo no catálogo do .NET")
    private Long protocoloId;

    @Schema(description = "Categoria do plano (PREVENTIVO ou POS_CIRURGICO)")
    private String categoria;

    @Schema(description = "Status atual do plano")
    private String statusPlano;

    @Schema(description = "Data/hora de instanciação do plano")
    private LocalDateTime instanciadoEm;

    @Schema(description = "Itens de origem PROTOCOLO já cumpridos")
    private List<ItemPlanoCuidadoDto> realizados;

    @Schema(description = "Itens de origem PROTOCOLO pendentes, com data-alvo ainda no futuro")
    private List<ItemPlanoCuidadoDto> pendentes;

    @Schema(description = "Itens de origem PROTOCOLO com data-alvo vencida (PENDENTE ou ATRASADO)")
    private List<ItemPlanoCuidadoDto> vencidos;

    public static ProtocoloAplicadoResponse from(PlanoCuidadoEntity plano) {
        ProtocoloAplicadoResponse r = new ProtocoloAplicadoResponse();
        r.planoId = plano.getId();
        r.animalId = plano.getAnimalId();
        r.protocoloId = plano.getProtocoloId();
        r.categoria = plano.getCategoria().name();
        r.statusPlano = plano.getStatus().name();
        r.instanciadoEm = plano.getCreatedAt();

        List<ItemPlanoCuidadoEntity> itensDoProtocolo = plano.getItens().stream()
            .filter(e -> e.getOrigem() == TipoOrigemItem.PROTOCOLO)
            .filter(e -> e.getStatus() != StatusItem.CANCELADO)
            .collect(Collectors.toList());

        r.realizados = itensDoProtocolo.stream()
            .filter(e -> e.getStatus() == StatusItem.REALIZADO)
            .map(ItemPlanoCuidadoDto::from)
            .collect(Collectors.toList());

        r.vencidos = itensDoProtocolo.stream()
            .map(ItemPlanoCuidadoDto::from)
            .filter(ItemPlanoCuidadoDto::isVencido)
            .collect(Collectors.toList());

        // pendente-nao-vencido: PENDENTE cuja data-alvo ainda nao passou
        r.pendentes = itensDoProtocolo.stream()
            .filter(e -> e.getStatus() == StatusItem.PENDENTE)
            .map(ItemPlanoCuidadoDto::from)
            .filter(dto -> !dto.isVencido())
            .collect(Collectors.toList());

        return r;
    }

    public Long getPlanoId() { return planoId; }
    public void setPlanoId(Long planoId) { this.planoId = planoId; }

    public Long getAnimalId() { return animalId; }
    public void setAnimalId(Long animalId) { this.animalId = animalId; }

    public Long getProtocoloId() { return protocoloId; }
    public void setProtocoloId(Long protocoloId) { this.protocoloId = protocoloId; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getStatusPlano() { return statusPlano; }
    public void setStatusPlano(String statusPlano) { this.statusPlano = statusPlano; }

    public LocalDateTime getInstanciadoEm() { return instanciadoEm; }
    public void setInstanciadoEm(LocalDateTime instanciadoEm) { this.instanciadoEm = instanciadoEm; }

    public List<ItemPlanoCuidadoDto> getRealizados() { return realizados; }
    public void setRealizados(List<ItemPlanoCuidadoDto> realizados) { this.realizados = realizados; }

    public List<ItemPlanoCuidadoDto> getPendentes() { return pendentes; }
    public void setPendentes(List<ItemPlanoCuidadoDto> pendentes) { this.pendentes = pendentes; }

    public List<ItemPlanoCuidadoDto> getVencidos() { return vencidos; }
    public void setVencidos(List<ItemPlanoCuidadoDto> vencidos) { this.vencidos = vencidos; }
}
