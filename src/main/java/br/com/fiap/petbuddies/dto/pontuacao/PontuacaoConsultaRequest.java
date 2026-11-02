package br.com.fiap.petbuddies.dto.pontuacao;

import br.com.fiap.petbuddies.domain.enums.atendimento.EvolucaoClinica;

import java.time.LocalDateTime;

/**
 * Corpo do POST /api/pontuacao/consulta no .NET: os fatos do período entre a
 * consulta anterior do animal e esta. As regras de pontuação e o cálculo ficam
 * do lado do .NET — os nomes dos campos são o contrato com o PontuarConsultaRequest de lá.
 */
public record PontuacaoConsultaRequest(
        Long consultaId,
        Long responsavelId,
        Long animalId,
        Long clinicaId,
        LocalDateTime dataConsulta,
        LocalDateTime dataConsultaAnterior,
        int dosesPrevistas,
        int dosesComCheckin,
        int diasNoPeriodo,
        int diasComCheckin,
        int acionamentosClinica,
        EvolucaoClinica evolucao
) {
}
