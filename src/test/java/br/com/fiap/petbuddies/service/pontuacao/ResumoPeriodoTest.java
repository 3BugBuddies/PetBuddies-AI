package br.com.fiap.petbuddies.service.pontuacao;

import br.com.fiap.petbuddies.domain.entity.ItemPlanoCuidadoEntity;
import br.com.fiap.petbuddies.domain.enums.cuidado.StatusItem;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResumoPeriodoTest {

    private static final LocalDate INICIO = LocalDate.of(2026, 9, 1);
    private static final LocalDate FIM = LocalDate.of(2026, 10, 1);

    private static ItemPlanoCuidadoEntity dose(StatusItem status) {
        ItemPlanoCuidadoEntity item = new ItemPlanoCuidadoEntity();
        item.setStatus(status);
        return item;
    }

    @Test
    void de_dosesComEDemCheckin_contaSoAsQueOTutorRelatou() {
        // REALIZADO, SUSPENSO e ENCAMINHADO só nascem de check-in; PENDENTE e ATRASADO não.
        List<ItemPlanoCuidadoEntity> doses = List.of(
                dose(StatusItem.REALIZADO),
                dose(StatusItem.SUSPENSO),
                dose(StatusItem.ENCAMINHADO),
                dose(StatusItem.PENDENTE),
                dose(StatusItem.ATRASADO));

        ResumoPeriodo resumo = ResumoPeriodo.de(INICIO, FIM, doses, List.of());

        assertEquals(5, resumo.dosesPrevistas());
        assertEquals(3, resumo.dosesComCheckin());
        assertEquals(1, resumo.acionamentosClinica());
    }

    @Test
    void de_periodoDeUmMes_contaOsDiasSemIncluirODiaDaConsulta() {
        ResumoPeriodo resumo = ResumoPeriodo.de(INICIO, FIM, List.of(), List.of());

        assertEquals(30, resumo.diasNoPeriodo());
    }

    @Test
    void de_checkinsRepetidosEForaDoPeriodo_contaSoOsDiasDistintosDentroDele() {
        List<LocalDate> dias = List.of(
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 15),
                FIM,                          // dia da consulta: fora, o intervalo é aberto no fim
                LocalDate.of(2026, 8, 31));   // antes do início

        ResumoPeriodo resumo = ResumoPeriodo.de(INICIO, FIM, List.of(), dias);

        assertEquals(2, resumo.diasComCheckin());
    }

    @Test
    void de_inicioDepoisDoFim_periodoVazioENaoNegativo() {
        ResumoPeriodo resumo = ResumoPeriodo.de(FIM.plusDays(10), FIM, List.of(), List.of());

        assertEquals(0, resumo.diasNoPeriodo());
        assertEquals(FIM, resumo.inicio());
    }
}
