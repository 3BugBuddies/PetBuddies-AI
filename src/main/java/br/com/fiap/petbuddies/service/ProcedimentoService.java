package br.com.fiap.petbuddies.service;

import br.com.fiap.petbuddies.domain.entity.AnimalEntity;
import br.com.fiap.petbuddies.domain.entity.ProcedimentoEntity;
import br.com.fiap.petbuddies.domain.entity.RegistroAtendimentoEntity;
import br.com.fiap.petbuddies.domain.entity.VeterinarioEntity;
import br.com.fiap.petbuddies.domain.enums.StatusProcedimento;
import br.com.fiap.petbuddies.domain.repository.AnimalRepository;
import br.com.fiap.petbuddies.domain.repository.ProcedimentoRepository;
import br.com.fiap.petbuddies.domain.repository.RegistroAtendimentoRepository;
import br.com.fiap.petbuddies.domain.repository.VeterinarioRepository;
import br.com.fiap.petbuddies.dto.ProcedimentoRequest;
import br.com.fiap.petbuddies.exception.AnimalNaoEncontradoException;
import br.com.fiap.petbuddies.exception.ProcedimentoNaoEncontradoException;
import br.com.fiap.petbuddies.exception.RegistroAtendimentoNaoEncontradoException;
import br.com.fiap.petbuddies.exception.VeterinarioNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProcedimentoService {

    private final ProcedimentoRepository repository;
    private final RegistroAtendimentoRepository registroAtendimentoRepository;
    private final AnimalRepository animalRepository;
    private final VeterinarioRepository veterinarioRepository;

    public ProcedimentoService(
            ProcedimentoRepository repository,
            RegistroAtendimentoRepository registroAtendimentoRepository,
            AnimalRepository animalRepository,
            VeterinarioRepository veterinarioRepository) {
        this.repository = repository;
        this.registroAtendimentoRepository = registroAtendimentoRepository;
        this.animalRepository = animalRepository;
        this.veterinarioRepository = veterinarioRepository;
    }

    @Transactional(readOnly = true)
    public List<ProcedimentoEntity> listar(Long animalId, Long registroAtendimentoId) {
        if (animalId != null) {
            return repository.findByAnimalIdOrderByDataPrevistaInicioDesc(animalId);
        }
        if (registroAtendimentoId != null) {
            return repository.findByRegistroAtendimentoId(registroAtendimentoId);
        }
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public ProcedimentoEntity buscarPorId(Long id) {
        return encontrarOuFalhar(id);
    }

    @Transactional
    public ProcedimentoEntity criar(ProcedimentoRequest request) {
        ProcedimentoEntity entity = new ProcedimentoEntity();
        aplicar(request, entity);
        if (request.getStatus() == null) {
            entity.setStatus(StatusProcedimento.PENDENTE);
        }
        return repository.save(entity);
    }

    @Transactional
    public ProcedimentoEntity atualizar(Long id, ProcedimentoRequest request) {
        ProcedimentoEntity entity = encontrarOuFalhar(id);
        aplicar(request, entity);
        return repository.save(entity);
    }

    @Transactional
    public void remover(Long id) {
        encontrarOuFalhar(id);
        repository.deleteById(id);
    }

    private ProcedimentoEntity encontrarOuFalhar(Long id) {
        return repository.findById(id).orElseThrow(() -> new ProcedimentoNaoEncontradoException(id));
    }

    private void aplicar(ProcedimentoRequest request, ProcedimentoEntity entity) {
        RegistroAtendimentoEntity registroAtendimento = registroAtendimentoRepository.findById(request.getRegistroAtendimentoId())
                .orElseThrow(() -> new RegistroAtendimentoNaoEncontradoException(request.getRegistroAtendimentoId()));
        AnimalEntity animal = animalRepository.findById(request.getAnimalId())
                .orElseThrow(() -> new AnimalNaoEncontradoException(request.getAnimalId()));
        VeterinarioEntity veterinario = veterinarioRepository.findById(request.getVeterinarioId())
                .orElseThrow(() -> new VeterinarioNaoEncontradoException(request.getVeterinarioId()));
        entity.setTipo(request.getTipo());
        entity.setNome(request.getNome());
        entity.setDescricao(request.getDescricao());
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
        entity.setDataPrevistaInicio(request.getDataPrevistaInicio());
        entity.setDataPrevistaFim(request.getDataPrevistaFim());
        entity.setAnexosUrl(request.getAnexosUrl());
        entity.setObservacao(request.getObservacao());
        entity.setRegistroAtendimento(registroAtendimento);
        entity.setAnimal(animal);
        entity.setVeterinario(veterinario);
    }
}
