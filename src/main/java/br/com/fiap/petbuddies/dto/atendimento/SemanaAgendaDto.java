package br.com.fiap.petbuddies.dto.atendimento;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** A grade da semana: uma linha por horário de início, uma célula por dia. */
public record SemanaAgendaDto(
        LocalDate segunda,
        LocalDate anterior,
        LocalDate proxima,
        List<LocalDate> dias,
        List<Linha> linhas,
        long livres) {

    /** FORA: sem janela naquele horário. PASSOU: janela livre que já começou. */
    public enum Estado { FORA, LIVRE, PASSOU, OCUPADA, REALIZADA }

    public record Celula(Estado estado, Long janelaId, ConsultaAgendaDto consulta) {
    }

    public record Linha(LocalTime hora, List<Celula> celulas) {
    }
}
