package br.com.fiap.petbuddies.service;

import br.com.fiap.petbuddies.domain.entity.AnimalEntity;
import br.com.fiap.petbuddies.domain.entity.ConsultaEntity;
import br.com.fiap.petbuddies.domain.entity.JanelaAtendimentoEntity;
import br.com.fiap.petbuddies.domain.entity.VeterinarioEntity;
import br.com.fiap.petbuddies.domain.enums.StatusConsulta;
import br.com.fiap.petbuddies.domain.repository.AnimalRepository;
import br.com.fiap.petbuddies.domain.repository.ConsultaRepository;
import br.com.fiap.petbuddies.domain.repository.JanelaAtendimentoRepository;
import br.com.fiap.petbuddies.domain.repository.VeterinarioRepository;
import br.com.fiap.petbuddies.dto.AgendamentoRequest;
import br.com.fiap.petbuddies.dto.CancelamentoRequest;
import br.com.fiap.petbuddies.dto.ConsultaRequest;
import br.com.fiap.petbuddies.exception.AnimalNaoEncontradoException;
import br.com.fiap.petbuddies.exception.ConsultaJaRealizadaException;
import br.com.fiap.petbuddies.exception.ConsultaNaoEncontradaException;
import br.com.fiap.petbuddies.exception.JanelaAtendimentoNaoEncontradaException;
import br.com.fiap.petbuddies.exception.JanelaConflitanteException;
import br.com.fiap.petbuddies.exception.JanelaNoPassadoException;
import br.com.fiap.petbuddies.exception.VeterinarioNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConsultaService {

    private final ConsultaRepository repository;
    private final AnimalRepository animalRepository;
    private final VeterinarioRepository veterinarioRepository;
    private final JanelaAtendimentoRepository janelaAtendimentoRepository;

    public ConsultaService(
            ConsultaRepository repository,
            AnimalRepository animalRepository,
            VeterinarioRepository veterinarioRepository,
            JanelaAtendimentoRepository janelaAtendimentoRepository) {
        this.repository = repository;
        this.animalRepository = animalRepository;
        this.veterinarioRepository = veterinarioRepository;
        this.janelaAtendimentoRepository = janelaAtendimentoRepository;
    }

    @Transactional(readOnly = true)
    public List<ConsultaEntity> listar(Long animalId, Long veterinarioId) {
        if (animalId != null) {
            return repository.findByAnimalIdOrderByDataHoraDesc(animalId);
        }
        if (veterinarioId != null) {
            return repository.findByVeterinarioIdOrderByDataHoraDesc(veterinarioId);
        }
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public ConsultaEntity buscarPorId(Long id) {
        return encontrarOuFalhar(id);
    }

    @Transactional
    public ConsultaEntity criar(ConsultaRequest request) {
        ConsultaEntity entity = new ConsultaEntity();
        aplicar(request, entity);
        if (request.getStatus() == null) {
            entity.setStatus(StatusConsulta.AGENDADA);
        }
        return repository.save(entity);
    }

    @Transactional
    public ConsultaEntity atualizar(Long id, ConsultaRequest request) {
        ConsultaEntity entity = encontrarOuFalhar(id);
        aplicar(request, entity);
        return repository.save(entity);
    }

    @Transactional
    public void remover(Long id) {
        encontrarOuFalhar(id);
        repository.deleteById(id);
    }

    // ocupa uma janela livre: a consulta herda data/hora e veterinário do slot, não do request
    @Transactional
    public ConsultaEntity agendar(AgendamentoRequest request) {
        JanelaAtendimentoEntity janela = janelaAtendimentoRepository.findById(request.getJanelaId())
                .orElseThrow(() -> new JanelaAtendimentoNaoEncontradaException(request.getJanelaId()));
        if (janela.getConsulta() != null) {
            throw new JanelaConflitanteException(janela.getVeterinario().getId(), janela.getDataHoraInicio());
        }
        if (janela.getDataHoraInicio().isBefore(LocalDateTime.now())) {
            throw new JanelaNoPassadoException(janela.getDataHoraInicio());
        }
        AnimalEntity animal = animalRepository.findById(request.getAnimalId())
                .orElseThrow(() -> new AnimalNaoEncontradoException(request.getAnimalId()));

        ConsultaEntity consulta = new ConsultaEntity();
        consulta.setTipo(request.getTipo());
        consulta.setDataHora(janela.getDataHoraInicio());
        consulta.setStatus(StatusConsulta.AGENDADA);
        consulta.setObservacao(request.getObservacao());
        consulta.setAnimal(animal);
        consulta.setVeterinario(janela.getVeterinario());
        consulta = repository.save(consulta);

        janela.setConsulta(consulta);
        janelaAtendimentoRepository.save(janela);
        return consulta;
    }

    // CK_CONSULTA_STATUS não impede reabrir REALIZADA; a regra de negócio, sim
    @Transactional
    public ConsultaEntity cancelar(Long id, CancelamentoRequest request) {
        ConsultaEntity consulta = encontrarOuFalhar(id);
        if (consulta.getStatus() == StatusConsulta.REALIZADA) {
            throw new ConsultaJaRealizadaException(id);
        }
        consulta.setStatus(StatusConsulta.CANCELADA);
        consulta.setMotivo(request.getMotivo());

        // FK_JANELA_CONSULTA só limpa em DELETE; cancelamento não é DELETE (ver JanelaAtendimentoEntity).
        janelaAtendimentoRepository.findByConsultaId(id)
                .ifPresent(janela -> janela.setConsulta(null));
        return repository.save(consulta);
    }

    private ConsultaEntity encontrarOuFalhar(Long id) {
        return repository.findById(id).orElseThrow(() -> new ConsultaNaoEncontradaException(id));
    }

    private void aplicar(ConsultaRequest request, ConsultaEntity entity) {
        AnimalEntity animal = animalRepository.findById(request.getAnimalId())
                .orElseThrow(() -> new AnimalNaoEncontradoException(request.getAnimalId()));
        VeterinarioEntity veterinario = veterinarioRepository.findById(request.getVeterinarioId())
                .orElseThrow(() -> new VeterinarioNaoEncontradoException(request.getVeterinarioId()));
        entity.setTipo(request.getTipo());
        entity.setDataHora(request.getDataHora());
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
        entity.setObservacao(request.getObservacao());
        entity.setMotivo(request.getMotivo());
        entity.setAnimal(animal);
        entity.setVeterinario(veterinario);
    }
}
