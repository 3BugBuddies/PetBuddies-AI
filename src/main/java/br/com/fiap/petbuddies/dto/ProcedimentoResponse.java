package br.com.fiap.petbuddies.dto;

import br.com.fiap.petbuddies.domain.entity.ProcedimentoEntity;
import br.com.fiap.petbuddies.domain.enums.StatusProcedimento;
import br.com.fiap.petbuddies.domain.enums.TipoProcedimento;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProcedimentoResponse {

    private Long id;
    private TipoProcedimento tipo;
    private String nome;
    private String descricao;
    private StatusProcedimento status;
    private LocalDateTime dataPrevistaInicio;
    private LocalDateTime dataPrevistaFim;
    private String anexosUrl;
    private String observacao;
    private Long registroAtendimentoId;
    private Long animalId;
    private Long veterinarioId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProcedimentoResponse from(ProcedimentoEntity entity) {
        ProcedimentoResponse dto = new ProcedimentoResponse();
        dto.id = entity.getId();
        dto.tipo = entity.getTipo();
        dto.nome = entity.getNome();
        dto.descricao = entity.getDescricao();
        dto.status = entity.getStatus();
        dto.dataPrevistaInicio = entity.getDataPrevistaInicio();
        dto.dataPrevistaFim = entity.getDataPrevistaFim();
        dto.anexosUrl = entity.getAnexosUrl();
        dto.observacao = entity.getObservacao();
        dto.registroAtendimentoId = entity.getRegistroAtendimento() == null ? null : entity.getRegistroAtendimento().getId();
        dto.animalId = entity.getAnimal() == null ? null : entity.getAnimal().getId();
        dto.veterinarioId = entity.getVeterinario() == null ? null : entity.getVeterinario().getId();
        dto.createdAt = entity.getCreatedAt();
        dto.updatedAt = entity.getUpdatedAt();
        return dto;
    }
}
