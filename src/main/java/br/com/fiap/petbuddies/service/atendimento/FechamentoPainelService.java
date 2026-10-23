package br.com.fiap.petbuddies.service.atendimento;

import br.com.fiap.petbuddies.domain.entity.ConsultaEntity;
import br.com.fiap.petbuddies.domain.enums.cuidado.TipoCuidado;
import br.com.fiap.petbuddies.dto.atendimento.CondicaoClinicaResponse;
import br.com.fiap.petbuddies.dto.atendimento.ConsultaResponse;
import br.com.fiap.petbuddies.dto.atendimento.FechamentoAtendimentoRequest;
import br.com.fiap.petbuddies.dto.atendimento.FechamentoAtendimentoResponse;
import br.com.fiap.petbuddies.dto.cuidado.ItemPlanoCuidadoDto;
import br.com.fiap.petbuddies.dto.prescricao.ReceitaModeloResponse;
import br.com.fiap.petbuddies.exception.atendimento.ConsultaNaoPodeSerFechadaException;
import br.com.fiap.petbuddies.service.cuidado.MotorPlanoService;
import br.com.fiap.petbuddies.service.prescricao.ReceitaModeloService;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/** O que a tela de fechar precisa antes de abrir, e o fechamento em si, que continua sendo do {@link FechamentoAtendimentoService}. */
@Service
public class FechamentoPainelService {

    private static final int LIMITE_ITENS_ABERTOS = 50;

    private final ConsultaService consultaService;
    private final MotorPlanoService motorPlanoService;
    private final CondicaoClinicaService condicaoClinicaService;
    private final ReceitaModeloService receitaModeloService;
    private final FechamentoAtendimentoService fechamentoAtendimentoService;

    public FechamentoPainelService(
            ConsultaService consultaService,
            MotorPlanoService motorPlanoService,
            CondicaoClinicaService condicaoClinicaService,
            ReceitaModeloService receitaModeloService,
            FechamentoAtendimentoService fechamentoAtendimentoService) {
        this.consultaService = consultaService;
        this.motorPlanoService = motorPlanoService;
        this.condicaoClinicaService = condicaoClinicaService;
        this.receitaModeloService = receitaModeloService;
        this.fechamentoAtendimentoService = fechamentoAtendimentoService;
    }

    /** Falha com conflito se a consulta já foi fechada ou cancelada: a tela nem abre. */
    public PreparoFechamento preparar(Long consultaId) {
        ConsultaEntity consulta = consultaService.buscarPorId(consultaId);
        if (!ConsultaService.STATUS_FECHAVEL.contains(consulta.getStatus())) {
            throw new ConsultaNaoPodeSerFechadaException(consultaId, consulta.getStatus());
        }
        Long animalId = consulta.getAnimal().getId();
        // Só vacina e vermífugo baixam item do plano; o mesmo critério do fechamento.
        List<ItemPlanoCuidadoDto> itensAbertos = motorPlanoService.listarEventos(
                animalId, List.of(TipoCuidado.VACINACAO, TipoCuidado.VERMIFUGACAO), true,
                PageRequest.of(0, LIMITE_ITENS_ABERTOS, Sort.by("dataAlvo"))).getContent();
        Long clinicaId = consulta.getVeterinario().getClinica().getId();
        List<CondicaoClinicaResponse> condicoes = condicaoClinicaService.listarAtivas(clinicaId);
        List<ReceitaModeloResponse> receitasModelo = receitaModeloService.listarAtivasDaClinica(clinicaId);
        return new PreparoFechamento(ConsultaResponse.from(consulta), animalId, consulta.getAnimal().getNome(),
                consulta.getAnimal().getPeso(), itensAbertos, condicoes, receitasModelo);
    }

    public FechamentoAtendimentoResponse fechar(Long consultaId, FechamentoAtendimentoRequest request) {
        return fechamentoAtendimentoService.fechar(consultaId, request);
    }

    public record PreparoFechamento(
            ConsultaResponse consulta,
            Long animalId,
            String animalNome,
            BigDecimal pesoAtual,
            List<ItemPlanoCuidadoDto> itensAbertos,
            List<CondicaoClinicaResponse> condicoes,
            List<ReceitaModeloResponse> receitasModelo) {
    }
}
