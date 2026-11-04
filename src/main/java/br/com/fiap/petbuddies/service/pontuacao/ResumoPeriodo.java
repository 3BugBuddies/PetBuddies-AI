package br.com.fiap.petbuddies.service.pontuacao;

import br.com.fiap.petbuddies.domain.entity.ItemPlanoCuidadoEntity;
import br.com.fiap.petbuddies.domain.enums.cuidado.StatusItem;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

/**
 * O que o tutor fez entre a consulta anterior e esta, no intervalo [inicio, fim).
 * Só conta; quanto isso vale em pontos é decisão do .NET.
 */
public record ResumoPeriodo(
        LocalDate inicio,
        LocalDate fim,
        int dosesPrevistas,
        int dosesComCheckin,
        int diasNoPeriodo,
        int diasComCheckin,
        int acionamentosClinica) {

    // Os três status que só o check-in grava: o tutor relatou, e o motor decidiu a dose, a suspensão ou o encaminhamento.
    private static final Set<StatusItem> COM_CHECKIN =
            EnumSet.of(StatusItem.REALIZADO, StatusItem.SUSPENSO, StatusItem.ENCAMINHADO);

    public static ResumoPeriodo de(LocalDate inicio, LocalDate fim,
                                   Collection<ItemPlanoCuidadoEntity> doses,
                                   Collection<LocalDate> diasComCheckin) {
        // Animal cadastrado depois da data da consulta (dado retroativo): período vazio, não negativo.
        LocalDate inicioValido = inicio.isAfter(fim) ? fim : inicio;

        int comCheckin = (int) doses.stream().filter(dose -> COM_CHECKIN.contains(dose.getStatus())).count();
        int acionamentos = (int) doses.stream().filter(dose -> dose.getStatus() == StatusItem.ENCAMINHADO).count();
        int diasRelatados = (int) diasComCheckin.stream()
                .filter(dia -> !dia.isBefore(inicioValido) && dia.isBefore(fim))
                .distinct()
                .count();

        return new ResumoPeriodo(
                inicioValido,
                fim,
                doses.size(),
                comCheckin,
                (int) ChronoUnit.DAYS.between(inicioValido, fim),
                diasRelatados,
                acionamentos);
    }
}
