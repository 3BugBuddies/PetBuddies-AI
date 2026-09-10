package br.com.fiap.petbuddies.dto.atendimento;

import br.com.fiap.petbuddies.domain.entity.ConsultaEntity;
import br.com.fiap.petbuddies.domain.entity.ProcedimentoEntity;
import br.com.fiap.petbuddies.domain.entity.RegistroAtendimentoEntity;
import br.com.fiap.petbuddies.domain.enums.atendimento.StatusConsulta;
import br.com.fiap.petbuddies.dto.prescricao.PrescricaoComRegrasResponse;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

/** O resultado do fechamento: a consulta já REALIZADA e tudo que a transação gravou. */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FechamentoAtendimentoResponse {

    private Long consultaId;
    private StatusConsulta status;
    private RegistroAtendimentoResponse registroAtendimento;
    private List<ProcedimentoResponse> procedimentos;
    private List<PrescricaoComRegrasResponse> prescricoes;

    public static FechamentoAtendimentoResponse from(
            ConsultaEntity consulta,
            RegistroAtendimentoEntity registroAtendimento,
            List<ProcedimentoEntity> procedimentos,
            List<PrescricaoComRegrasResponse> prescricoes) {
        FechamentoAtendimentoResponse dto = new FechamentoAtendimentoResponse();
        dto.consultaId = consulta.getId();
        dto.status = consulta.getStatus();
        dto.registroAtendimento = RegistroAtendimentoResponse.from(registroAtendimento);
        dto.procedimentos = procedimentos.stream().map(ProcedimentoResponse::from).collect(Collectors.toList());
        dto.prescricoes = prescricoes;
        return dto;
    }
}
