package br.com.fiap.petbuddies.dto;

import br.com.fiap.petbuddies.domain.entity.ItemPlanoCuidadoEntity;
import br.com.fiap.petbuddies.domain.enums.StatusItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDate;

@Schema(description = "Evento de um plano de cuidado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemPlanoCuidadoDto {

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

    @Schema(description = "true quando a data-alvo já passou e o item não foi cumprido (PENDENTE ou ATRASADO). "
        + "Derivado na leitura: nada no sistema hoje transiciona um item para ATRASADO")
    private boolean vencido;

    public static ItemPlanoCuidadoDto from(ItemPlanoCuidadoEntity e) {
        ItemPlanoCuidadoDto dto = new ItemPlanoCuidadoDto();
        dto.id = e.getId();
        dto.tipo = e.getTipo().name();
        dto.nome = e.getNome();
        dto.dataAlvo = e.getDataAlvo();
        dto.status = e.getStatus().name();
        dto.origem = e.getOrigem() != null ? e.getOrigem().name() : null;
        dto.prescricaoId = e.getPrescricaoId();
        dto.vencido = isVencido(e);
        return dto;
    }

    private static boolean isVencido(ItemPlanoCuidadoEntity e) {
        if (e.getStatus() == StatusItem.ATRASADO) {
            return true;
        }
        return e.getStatus() == StatusItem.PENDENTE
            && e.getDataAlvo() != null
            && e.getDataAlvo().isBefore(LocalDate.now());
    }
}
