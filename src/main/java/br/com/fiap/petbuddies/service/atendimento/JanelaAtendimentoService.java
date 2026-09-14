package br.com.fiap.petbuddies.service.atendimento;

import br.com.fiap.petbuddies.domain.entity.ConsultaEntity;
import br.com.fiap.petbuddies.domain.entity.JanelaAtendimentoEntity;
import br.com.fiap.petbuddies.domain.entity.VeterinarioEntity;
import br.com.fiap.petbuddies.domain.repository.ConsultaRepository;
import br.com.fiap.petbuddies.domain.repository.JanelaAtendimentoRepository;
import br.com.fiap.petbuddies.domain.repository.VeterinarioRepository;
import br.com.fiap.petbuddies.dto.atendimento.JanelaAtendimentoRequest;
import br.com.fiap.petbuddies.exception.atendimento.ConsultaJaEmOutraJanelaException;
import br.com.fiap.petbuddies.exception.atendimento.ConsultaNaoEncontradaException;
import br.com.fiap.petbuddies.exception.atendimento.JanelaAtendimentoNaoEncontradaException;
import br.com.fiap.petbuddies.exception.atendimento.JanelaConflitanteException;
import br.com.fiap.petbuddies.exception.cadastro.VeterinarioNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class JanelaAtendimentoService {

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
