package br.com.fiap.petbuddies.dto;

import br.com.fiap.petbuddies.domain.entity.JanelaAtendimentoEntity;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class JanelaAtendimentoResponse {

    private Long id;
    private LocalDateTime dataHoraInicio;
    private Long veterinarioId;
    private Long consultaId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static JanelaAtendimentoResponse from(JanelaAtendimentoEntity entity) {
        JanelaAtendimentoResponse dto = new JanelaAtendimentoResponse();
        dto.id = entity.getId();
        dto.dataHoraInicio = entity.getDataHoraInicio();
        // Só os ids: com open-in-view=false, ler outro campo do proxy LAZY aqui lança LazyInitializationException.
        dto.veterinarioId = entity.getVeterinario() == null ? null : entity.getVeterinario().getId();
        dto.consultaId = entity.getConsulta() == null ? null : entity.getConsulta().getId();
        dto.createdAt = entity.getCreatedAt();
        dto.updatedAt = entity.getUpdatedAt();
        return dto;
    }
}
