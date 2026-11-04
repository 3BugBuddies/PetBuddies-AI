package br.com.fiap.petbuddies.dto.atendimento;

import br.com.fiap.petbuddies.domain.entity.ConsultaEntity;
import br.com.fiap.petbuddies.domain.enums.atendimento.StatusConsulta;
import br.com.fiap.petbuddies.domain.enums.atendimento.TipoConsulta;
import java.time.LocalDateTime;

/** Uma consulta como a agenda e o início a mostram. Exige animal e responsável já carregados. */
public record ConsultaAgendaDto(
        Long id,
        LocalDateTime dataHora,
        TipoConsulta tipo,
        StatusConsulta status,
        Long animalId,
        String animalNome,
        String responsavelNome,
        boolean fechavel) {

    public static ConsultaAgendaDto from(ConsultaEntity consulta, boolean fechavel) {
        return new ConsultaAgendaDto(
                consulta.getId(),
                consulta.getDataHora(),
                consulta.getTipo(),
                consulta.getStatus(),
                consulta.getAnimal().getId(),
                consulta.getAnimal().getNome(),
                consulta.getAnimal().getResponsavel().getNome(),
                fechavel);
    }
}
