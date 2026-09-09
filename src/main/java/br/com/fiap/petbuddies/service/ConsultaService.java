package br.com.fiap.petbuddies.service;

import br.com.fiap.petbuddies.domain.entity.AnimalEntity;
import br.com.fiap.petbuddies.domain.entity.ConsultaEntity;
import br.com.fiap.petbuddies.domain.entity.VeterinarioEntity;
import br.com.fiap.petbuddies.domain.enums.StatusConsulta;
import br.com.fiap.petbuddies.domain.repository.AnimalRepository;
import br.com.fiap.petbuddies.domain.repository.ConsultaRepository;
import br.com.fiap.petbuddies.domain.repository.VeterinarioRepository;
import br.com.fiap.petbuddies.dto.ConsultaRequest;
import br.com.fiap.petbuddies.exception.AnimalNaoEncontradoException;
import br.com.fiap.petbuddies.exception.ConsultaNaoEncontradaException;
import br.com.fiap.petbuddies.exception.VeterinarioNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConsultaService {

    private final ConsultaRepository repository;
    private final AnimalRepository animalRepository;
    private final VeterinarioRepository veterinarioRepository;

    public ConsultaService(
            ConsultaRepository repository,
            AnimalRepository animalRepository,
            VeterinarioRepository veterinarioRepository) {
        this.repository = repository;
        this.animalRepository = animalRepository;
        this.veterinarioRepository = veterinarioRepository;
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
