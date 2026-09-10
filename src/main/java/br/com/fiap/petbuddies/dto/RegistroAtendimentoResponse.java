package br.com.fiap.petbuddies.dto;

import br.com.fiap.petbuddies.domain.entity.RegistroAtendimentoEntity;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RegistroAtendimentoResponse {

    private Long id;
    private LocalDateTime dataAtendimento;
    private String anamnese;
    private String diagnostico;
    private String tratamento;
    private String observacao;
    private LocalDate proximoRetorno;
    private LocalDate proximaVacina;
    private Long animalId;
    private Long consultaId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RegistroAtendimentoResponse from(RegistroAtendimentoEntity entity) {
        RegistroAtendimentoResponse dto = new RegistroAtendimentoResponse();
        dto.id = entity.getId();
        dto.dataAtendimento = entity.getDataAtendimento();
        dto.anamnese = entity.getAnamnese();
        dto.diagnostico = entity.getDiagnostico();
        dto.tratamento = entity.getTratamento();
        dto.observacao = entity.getObservacao();
        dto.proximoRetorno = entity.getProximoRetorno();
        dto.proximaVacina = entity.getProximaVacina();
        dto.animalId = entity.getAnimal() == null ? null : entity.getAnimal().getId();
        dto.consultaId = entity.getConsulta() == null ? null : entity.getConsulta().getId();
        dto.createdAt = entity.getCreatedAt();
        dto.updatedAt = entity.getUpdatedAt();
        return dto;
    }
}
