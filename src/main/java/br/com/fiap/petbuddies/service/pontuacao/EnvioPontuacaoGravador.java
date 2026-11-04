package br.com.fiap.petbuddies.service.pontuacao;

import br.com.fiap.petbuddies.domain.entity.AnimalEntity;
import br.com.fiap.petbuddies.domain.entity.ConsultaEntity;
import br.com.fiap.petbuddies.domain.entity.EnvioPontuacaoEntity;
import br.com.fiap.petbuddies.domain.entity.ItemPlanoCuidadoEntity;
import br.com.fiap.petbuddies.domain.entity.RegistroAtendimentoEntity;
import br.com.fiap.petbuddies.domain.enums.atendimento.EvolucaoClinica;
import br.com.fiap.petbuddies.domain.enums.atendimento.StatusConsulta;
import br.com.fiap.petbuddies.domain.enums.cuidado.TipoOrigemItem;
import br.com.fiap.petbuddies.domain.enums.pontuacao.StatusEnvioPontuacao;
import br.com.fiap.petbuddies.domain.repository.CheckinRepository;
import br.com.fiap.petbuddies.domain.repository.ConsultaRepository;
import br.com.fiap.petbuddies.domain.repository.EnvioPontuacaoRepository;
import br.com.fiap.petbuddies.domain.repository.ItemPlanoCuidadoRepository;
import br.com.fiap.petbuddies.domain.repository.RegistroAtendimentoRepository;
import br.com.fiap.petbuddies.dto.pontuacao.PontuacaoConsultaRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Toda ida ao banco do envio, em transações curtas. O PontuacaoService chama o
 * .NET entre elas: a chamada HTTP não segura conexão do pool (mesmo motivo do MotorPlanoService).
 */
@Component
public class EnvioPontuacaoGravador {

    private static final int TAMANHO_ERRO = 500;

    private final EnvioPontuacaoRepository envioRepository;
    private final ConsultaRepository consultaRepository;
    private final ItemPlanoCuidadoRepository itemPlanoRepository;
    private final CheckinRepository checkinRepository;
    private final RegistroAtendimentoRepository registroAtendimentoRepository;

    public EnvioPontuacaoGravador(
            EnvioPontuacaoRepository envioRepository,
            ConsultaRepository consultaRepository,
            ItemPlanoCuidadoRepository itemPlanoRepository,
            CheckinRepository checkinRepository,
            RegistroAtendimentoRepository registroAtendimentoRepository) {
        this.envioRepository = envioRepository;
        this.consultaRepository = consultaRepository;
        this.itemPlanoRepository = itemPlanoRepository;
        this.checkinRepository = checkinRepository;
        this.registroAtendimentoRepository = registroAtendimentoRepository;
    }

    // REQUIRES_NEW: chamado também do listener AFTER_COMMIT, quando a transação do fechamento já fechou.
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public Optional<PontuacaoConsultaRequest> montarRequisicao(Long consultaId) {
        boolean pendente = envioRepository.findByConsultaId(consultaId)
                .map(envio -> envio.getStatus() == StatusEnvioPontuacao.PENDENTE)
                .orElse(false);
        if (!pendente) {
            return Optional.empty();
        }

        return consultaRepository.findParaPontuacao(consultaId).map(this::montar);
    }

    @Transactional(readOnly = true)
    public List<Long> pendentes() {
        return envioRepository.findConsultaIdsPorStatus(StatusEnvioPontuacao.PENDENTE);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void marcarEnviado(Long consultaId) {
        envioRepository.findByConsultaId(consultaId).ifPresent(envio -> {
            envio.setStatus(StatusEnvioPontuacao.ENVIADO);
            envio.setTentativas(envio.getTentativas() + 1);
            envio.setUltimoErro(null);
            envio.setEnviadoEm(LocalDateTime.now());
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void marcarRecusado(Long consultaId, String erro) {
        envioRepository.findByConsultaId(consultaId).ifPresent(envio -> {
            envio.setStatus(StatusEnvioPontuacao.RECUSADO);
            registrarTentativa(envio, erro);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarFalha(Long consultaId, String erro) {
        envioRepository.findByConsultaId(consultaId).ifPresent(envio -> registrarTentativa(envio, erro));
    }

    private PontuacaoConsultaRequest montar(ConsultaEntity consulta) {
        AnimalEntity animal = consulta.getAnimal();
        LocalDateTime anterior = consultaRepository.findDataUltimaComStatusAntes(
                animal.getId(), StatusConsulta.REALIZADA, consulta.getDataHora());

        // Primeira consulta realizada: o período começa no cadastro do animal.
        LocalDate fim = consulta.getDataHora().toLocalDate();
        LocalDate inicio = anterior != null
                ? anterior.toLocalDate()
                : animal.getAuditoria().getCreatedAt().toLocalDate();

        List<ItemPlanoCuidadoEntity> doses = itemPlanoRepository.findPorOrigemNoPeriodo(
                animal.getId(), TipoOrigemItem.PRESCRICAO, inicio, fim);
        List<LocalDate> diasComCheckin = checkinRepository.findDiasComCheckin(animal.getId(), inicio, fim);
        ResumoPeriodo resumo = ResumoPeriodo.de(inicio, fim, doses, diasComCheckin);

        EvolucaoClinica evolucao = registroAtendimentoRepository.findByConsultaId(consulta.getId()).stream()
                .map(RegistroAtendimentoEntity::getEvolucao)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);

        return new PontuacaoConsultaRequest(
                consulta.getId(),
                animal.getResponsavel().getId(),
                animal.getId(),
                consulta.getVeterinario().getClinica().getId(),
                consulta.getDataHora(),
                anterior,
                resumo.dosesPrevistas(),
                resumo.dosesComCheckin(),
                resumo.diasNoPeriodo(),
                resumo.diasComCheckin(),
                resumo.acionamentosClinica(),
                evolucao);
    }

    private static void registrarTentativa(EnvioPontuacaoEntity envio, String erro) {
        envio.setTentativas(envio.getTentativas() + 1);
        envio.setUltimoErro(erro == null || erro.length() <= TAMANHO_ERRO ? erro : erro.substring(0, TAMANHO_ERRO));
    }
}
