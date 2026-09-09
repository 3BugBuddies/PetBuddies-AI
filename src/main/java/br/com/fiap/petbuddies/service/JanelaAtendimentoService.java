package br.com.fiap.petbuddies.service;

import br.com.fiap.petbuddies.domain.entity.ConsultaEntity;
import br.com.fiap.petbuddies.domain.entity.JanelaAtendimentoEntity;
import br.com.fiap.petbuddies.domain.entity.VeterinarioEntity;
import br.com.fiap.petbuddies.domain.repository.ConsultaRepository;
import br.com.fiap.petbuddies.domain.repository.JanelaAtendimentoRepository;
import br.com.fiap.petbuddies.domain.repository.VeterinarioRepository;
import br.com.fiap.petbuddies.dto.JanelaAtendimentoRequest;
import br.com.fiap.petbuddies.exception.ConsultaNaoEncontradaException;
import br.com.fiap.petbuddies.exception.JanelaAtendimentoNaoEncontradaException;
import br.com.fiap.petbuddies.exception.JanelaConflitanteException;
import br.com.fiap.petbuddies.exception.VeterinarioNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public JanelaAtendimentoEntity criar(JanelaAtendimentoRequest request) {
        if (repository.existsByVeterinarioIdAndDataHoraInicio(request.getVeterinarioId(), request.getDataHoraInicio())) {
            throw new JanelaConflitanteException(request.getVeterinarioId(), request.getDataHoraInicio());
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
