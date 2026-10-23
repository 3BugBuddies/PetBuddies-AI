package br.com.fiap.petbuddies.service.atendimento;

import br.com.fiap.petbuddies.domain.entity.ConsultaEntity;
import br.com.fiap.petbuddies.domain.entity.JanelaAtendimentoEntity;
import br.com.fiap.petbuddies.domain.entity.VeterinarioEntity;
import br.com.fiap.petbuddies.domain.repository.ConsultaRepository;
import br.com.fiap.petbuddies.domain.repository.JanelaAtendimentoRepository;
import br.com.fiap.petbuddies.domain.repository.VeterinarioRepository;
import br.com.fiap.petbuddies.domain.enums.atendimento.StatusConsulta;
import br.com.fiap.petbuddies.dto.atendimento.ConsultaAgendaDto;
import br.com.fiap.petbuddies.dto.atendimento.JanelaAtendimentoRequest;
import br.com.fiap.petbuddies.dto.atendimento.JanelaLivreDto;
import br.com.fiap.petbuddies.dto.atendimento.SemanaAgendaDto;
import br.com.fiap.petbuddies.dto.atendimento.SemanaAgendaDto.Celula;
import br.com.fiap.petbuddies.dto.atendimento.SemanaAgendaDto.Estado;
import br.com.fiap.petbuddies.dto.atendimento.SemanaAgendaDto.Linha;
import br.com.fiap.petbuddies.exception.atendimento.ConsultaJaEmOutraJanelaException;
import br.com.fiap.petbuddies.exception.atendimento.ConsultaNaoEncontradaException;
import br.com.fiap.petbuddies.exception.atendimento.JanelaAtendimentoNaoEncontradaException;
import br.com.fiap.petbuddies.exception.atendimento.JanelaConflitanteException;
import br.com.fiap.petbuddies.exception.atendimento.PeriodoInvalidoException;
import br.com.fiap.petbuddies.exception.cadastro.VeterinarioNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Service
public class JanelaAtendimentoService {

    private static final long MAX_DIAS_PERIODO = 62;

    private final JanelaAtendimentoRepository repository;
    private final VeterinarioRepository veterinarioRepository;
    private final ConsultaRepository consultaRepository;

    public JanelaAtendimentoService(
            JanelaAtendimentoRepository repository,
            VeterinarioRepository veterinarioRepository,
            ConsultaRepository consultaRepository) {
        this.repository = repository;
        this.veterinarioRepository = veterinarioRepository;
        this.consultaRepository = consultaRepository;
    }

    @Transactional(readOnly = true)
    public List<JanelaAtendimentoEntity> listar(Long veterinarioId) {
        if (veterinarioId != null) {
            return repository.findByVeterinarioIdOrderByDataHoraInicioAsc(veterinarioId);
        }
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public JanelaAtendimentoEntity buscarPorId(Long id) {
        return encontrarOuFalhar(id);
    }

    // a leitura que a tela de agendamento faz primeiro: slots livres de um vet, num dia
    @Transactional(readOnly = true)
    public List<JanelaAtendimentoEntity> listarLivres(Long veterinarioId, LocalDate data) {
        LocalDateTime inicioDoDia = data.atStartOfDay();
        LocalDateTime agora = LocalDateTime.now();
        // mesma regra do agendamento, que recusa janela ja iniciada
        LocalDateTime inicio = inicioDoDia.isAfter(agora) ? inicioDoDia : agora;
        return repository.findLivresNoPeriodo(veterinarioId, inicio, inicioDoDia.plusDays(1));
    }

    // calendario do agendamento: so os dias com ao menos um slot livre ficam clicaveis
    @Transactional(readOnly = true)
    public List<LocalDate> listarDatasLivres(Long veterinarioId, LocalDate de, LocalDate ate) {
        if (ate.isBefore(de)) {
            throw new PeriodoInvalidoException("O fim do período (ate) não pode ser anterior ao início (de).");
        }
        if (ChronoUnit.DAYS.between(de, ate) > MAX_DIAS_PERIODO) {
            throw new PeriodoInvalidoException("O período não pode passar de " + MAX_DIAS_PERIODO + " dias.");
        }
        LocalDateTime inicioDoPeriodo = de.atStartOfDay();
        LocalDateTime agora = LocalDateTime.now();
        // mesma regra do agendamento, que recusa janela ja iniciada
        LocalDateTime inicio = inicioDoPeriodo.isAfter(agora) ? inicioDoPeriodo : agora;
        return repository.findLivresNoPeriodo(veterinarioId, inicio, ate.plusDays(1).atStartOfDay()).stream()
                .map(j -> j.getDataHoraInicio().toLocalDate())
                .distinct()
                .toList();
    }

    /** Horários abertos de toda a clínica, a partir de agora, para a tela de agendar. */
    @Transactional(readOnly = true)
    public List<JanelaLivreDto> listarLivresDaClinica(LocalDate de, LocalDate ate) {
        LocalDateTime inicioDoPeriodo = de.atStartOfDay();
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime inicio = inicioDoPeriodo.isAfter(agora) ? inicioDoPeriodo : agora;
        return repository.findLivresDaClinica(inicio, ate.plusDays(1).atStartOfDay()).stream()
                .map(j -> new JanelaLivreDto(j.getId(), j.getDataHoraInicio(),
                        j.getVeterinario().getId(), j.getVeterinario().getNome()))
                .toList();
    }

    /**
     * A grade de um veterinário numa semana (segunda a sábado, domingo só se tiver janela). Uma consulta
     * sem janela também aparece, para a veterinária nunca perder uma consulta marcada fora do fluxo novo.
     */
    @Transactional(readOnly = true)
    public SemanaAgendaDto semana(Long veterinarioId, LocalDate segunda) {
        LocalDateTime inicio = segunda.atStartOfDay();
        LocalDateTime fim = segunda.plusDays(7).atStartOfDay();
        LocalDateTime agora = LocalDateTime.now();

        Map<LocalDateTime, Celula> celulas = new HashMap<>();
        Set<Long> consultasComJanela = new HashSet<>();
        long livres = 0;
        for (JanelaAtendimentoEntity janela : repository.findDaSemana(veterinarioId, inicio, fim)) {
            ConsultaEntity consulta = janela.getConsulta();
            if (consulta == null) {
                boolean passou = janela.getDataHoraInicio().isBefore(agora);
                celulas.put(janela.getDataHoraInicio(),
                        new Celula(passou ? Estado.PASSOU : Estado.LIVRE, janela.getId(), null));
                livres += passou ? 0 : 1;
            } else {
                consultasComJanela.add(consulta.getId());
                celulas.put(janela.getDataHoraInicio(), celulaDaConsulta(janela.getId(), consulta));
            }
        }
        for (ConsultaEntity consulta : consultaRepository.findNoPeriodo(inicio, fim)) {
            boolean ativa = consulta.getStatus() != StatusConsulta.CANCELADA;
            if (ativa && consulta.getVeterinario().getId().equals(veterinarioId)
                    && !consultasComJanela.contains(consulta.getId())) {
                celulas.putIfAbsent(consulta.getDataHora(), celulaDaConsulta(null, consulta));
            }
        }

        boolean temDomingo = celulas.keySet().stream().anyMatch(d -> d.getDayOfWeek() == DayOfWeek.SUNDAY);
        List<LocalDate> dias = new ArrayList<>();
        for (int i = 0; i < (temDomingo ? 7 : 6); i++) {
            dias.add(segunda.plusDays(i));
        }
        TreeSet<LocalTime> horas = new TreeSet<>();
        celulas.keySet().forEach(d -> horas.add(d.toLocalTime()));
        List<Linha> linhas = horas.stream()
                .map(hora -> new Linha(hora, dias.stream()
                        .map(dia -> celulas.getOrDefault(dia.atTime(hora), new Celula(Estado.FORA, null, null)))
                        .toList()))
                .toList();
        return new SemanaAgendaDto(segunda, segunda.minusWeeks(1), segunda.plusWeeks(1), dias, linhas, livres);
    }

    private Celula celulaDaConsulta(Long janelaId, ConsultaEntity consulta) {
        Estado estado = consulta.getStatus() == StatusConsulta.REALIZADA ? Estado.REALIZADA : Estado.OCUPADA;
        return new Celula(estado, janelaId,
                ConsultaAgendaDto.from(consulta, ConsultaService.STATUS_FECHAVEL.contains(consulta.getStatus())));
    }

    @Transactional
    public JanelaAtendimentoEntity criar(JanelaAtendimentoRequest request) {
        if (repository.existsByVeterinarioIdAndDataHoraInicio(request.getVeterinarioId(), request.getDataHoraInicio())) {
            throw new JanelaConflitanteException(request.getVeterinarioId(), request.getDataHoraInicio());
        }
        if (request.getConsultaId() != null && repository.existsByConsultaId(request.getConsultaId())) {
            throw new ConsultaJaEmOutraJanelaException(request.getConsultaId());
        }
        JanelaAtendimentoEntity entity = new JanelaAtendimentoEntity();
        aplicar(request, entity);
        return repository.save(entity);
    }

    @Transactional
    public JanelaAtendimentoEntity atualizar(Long id, JanelaAtendimentoRequest request) {
        JanelaAtendimentoEntity entity = encontrarOuFalhar(id);
        if (repository.existsByVeterinarioIdAndDataHoraInicioAndIdNot(
                request.getVeterinarioId(), request.getDataHoraInicio(), id)) {
            throw new JanelaConflitanteException(request.getVeterinarioId(), request.getDataHoraInicio());
        }
        // AndIdNot exclui a propria janela: reenviar a mesma consultaId nao pode virar 409.
        if (request.getConsultaId() != null && repository.existsByConsultaIdAndIdNot(request.getConsultaId(), id)) {
            throw new ConsultaJaEmOutraJanelaException(request.getConsultaId());
        }
        aplicar(request, entity);
        return repository.save(entity);
    }

    @Transactional
    public void remover(Long id) {
        encontrarOuFalhar(id);
        repository.deleteById(id);
    }

    private JanelaAtendimentoEntity encontrarOuFalhar(Long id) {
        return repository.findById(id).orElseThrow(() -> new JanelaAtendimentoNaoEncontradaException(id));
    }

    private void aplicar(JanelaAtendimentoRequest request, JanelaAtendimentoEntity entity) {
        VeterinarioEntity veterinario = veterinarioRepository.findById(request.getVeterinarioId())
                .orElseThrow(() -> new VeterinarioNaoEncontradoException(request.getVeterinarioId()));
        ConsultaEntity consulta = null;
        if (request.getConsultaId() != null) {
            consulta = consultaRepository.findById(request.getConsultaId())
                    .orElseThrow(() -> new ConsultaNaoEncontradaException(request.getConsultaId()));
        }
        entity.setDataHoraInicio(request.getDataHoraInicio());
        entity.setVeterinario(veterinario);
        entity.setConsulta(consulta);
    }
}
