package br.com.fiap.petbuddies.dto;

import br.com.fiap.petbuddies.domain.entity.ConsultaEntity;
import br.com.fiap.petbuddies.domain.enums.StatusConsulta;
import br.com.fiap.petbuddies.domain.enums.TipoConsulta;
import lombok.*;

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
        // Só os ids: com open-in-view=false, ler outro campo do proxy LAZY aqui lança LazyInitializationException.
        dto.animalId = entity.getAnimal() == null ? null : entity.getAnimal().getId();
        dto.veterinarioId = entity.getVeterinario() == null ? null : entity.getVeterinario().getId();
        dto.createdAt = entity.getCreatedAt();
        dto.updatedAt = entity.getUpdatedAt();
        return dto;
    }
}
