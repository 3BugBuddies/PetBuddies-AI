package br.com.fiap.petbuddies.dto.pontuacao;

import br.com.fiap.petbuddies.domain.enums.atendimento.EvolucaoClinica;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Como os pontos de uma consulta foram calculados, lido do PetBuddies-API (.NET),
 * que guarda a memória no MongoDB. As datas vindas de lá chegam em UTC.
 */
public record MemoriaCalculoResponse(
        Long consultaId,
        Long lancamentoId,
        Long responsavelId,
        Long animalId,
        Long clinicaId,
        LocalDateTime periodoDe,
        LocalDateTime periodoAte,
        int dosesPrevistas,
        int dosesComCheckin,
        int diasNoPeriodo,
        int diasComCheckin,
        int acionamentosClinica,
        EvolucaoClinica evolucao,
        List<Parcela> parcelas,
        int total,
        LocalDateTime calculadoEm) {

    /** Quanto um critério (gesto da regra de pontuação) rendeu. */
    public record Parcela(
            String gesto,
            int quantidade,
            int pontosPorUnidade,
            int pontos,
            Long regraId,
            LocalDate inicioVigencia,
            String observacao) {
    }
}
