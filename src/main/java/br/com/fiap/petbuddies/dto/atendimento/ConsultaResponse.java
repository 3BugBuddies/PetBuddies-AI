package br.com.fiap.petbuddies.dto.atendimento;

import br.com.fiap.petbuddies.domain.entity.ConsultaEntity;
import br.com.fiap.petbuddies.domain.enums.atendimento.StatusConsulta;
import br.com.fiap.petbuddies.domain.enums.atendimento.TipoConsulta;
import lombok.*;
import org.hibernate.Hibernate;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaResponse {

    private Long id;
    private TipoConsulta tipo;
    private LocalDateTime dataHora;
    private StatusConsulta status;
    private String observacao;
    private String motivo;
    private Long animalId;
    private Long veterinarioId;
    private String animalNome;
    private String veterinarioNome;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ConsultaResponse from(ConsultaEntity entity) {
        ConsultaResponse dto = new ConsultaResponse();
        dto.id = entity.getId();
        dto.tipo = entity.getTipo();
        dto.dataHora = entity.getDataHora();
        dto.status = entity.getStatus();
        dto.observacao = entity.getObservacao();
        dto.motivo = entity.getMotivo();
        dto.animalId = entity.getAnimal() == null ? null : entity.getAnimal().getId();
        dto.veterinarioId = entity.getVeterinario() == null ? null : entity.getVeterinario().getId();
        // Com open-in-view=false, proxy LAZY não inicializado lançaria LazyInitializationException; a leitura precisa do JOIN FETCH.
        if (entity.getAnimal() != null && Hibernate.isInitialized(entity.getAnimal())) {
            dto.animalNome = entity.getAnimal().getNome();
        }
        if (entity.getVeterinario() != null && Hibernate.isInitialized(entity.getVeterinario())) {
            dto.veterinarioNome = entity.getVeterinario().getNome();
        }
        dto.createdAt = entity.getAuditoria().getCreatedAt();
        dto.updatedAt = entity.getAuditoria().getUpdatedAt();
        return dto;
    }
}
