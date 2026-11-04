package br.com.fiap.petbuddies.dto.atendimento;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

/** O modelo semanal de horários de uma veterinária. Não é gravado: só gera as janelas. */
public record ModeloSemanal(List<Faixa> faixas, int duracaoMinutos, int semanas, Set<LocalDate> bloqueados) {

    public record Faixa(DayOfWeek dia, LocalTime inicio, LocalTime fim) {
    }
}
