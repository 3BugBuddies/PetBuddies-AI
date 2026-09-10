package br.com.fiap.petbuddies.dto;

import br.com.fiap.petbuddies.domain.entity.ConsultaEntity;
import br.com.fiap.petbuddies.domain.entity.ProcedimentoEntity;
import br.com.fiap.petbuddies.domain.entity.RegistroAtendimentoEntity;
import br.com.fiap.petbuddies.domain.enums.StatusConsulta;

import java.util.List;
import java.util.stream.Collectors;

/** O resultado do fechamento: a consulta já REALIZADA e tudo que a transação gravou. */
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

    public Long getConsultaId() { return consultaId; }
    public StatusConsulta getStatus() { return status; }
    public RegistroAtendimentoResponse getRegistroAtendimento() { return registroAtendimento; }
    public List<ProcedimentoResponse> getProcedimentos() { return procedimentos; }
    public List<PrescricaoComRegrasResponse> getPrescricoes() { return prescricoes; }
}
