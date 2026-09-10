package br.com.fiap.petbuddies.service.cadastro;

import br.com.fiap.petbuddies.domain.entity.AnimalEntity;
import br.com.fiap.petbuddies.domain.entity.ResponsavelEntity;
import br.com.fiap.petbuddies.domain.repository.AnimalRepository;
import br.com.fiap.petbuddies.domain.repository.ResponsavelRepository;
import br.com.fiap.petbuddies.dto.cadastro.AnimalRequest;
import br.com.fiap.petbuddies.exception.cadastro.AnimalNaoEncontradoException;
import br.com.fiap.petbuddies.exception.cadastro.ResponsavelNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AnimalService {

    /** Default de RC_RACA no DDL. */
    private static final String RACA_PADRAO = "SEM_RACA";

    private final AnimalRepository repository;
    private final ResponsavelRepository responsavelRepository;

    public AnimalService(AnimalRepository repository, ResponsavelRepository responsavelRepository) {
        this.repository = repository;
        this.responsavelRepository = responsavelRepository;
    }

    @Transactional(readOnly = true)
    public List<AnimalEntity> listar(Long responsavelId, String nome) {
        if (responsavelId != null) {
            return repository.findByResponsavelId(responsavelId);
        }
        if (nome != null && !nome.isBlank()) {
            return repository.findByNomeContainingIgnoreCase(nome);
        }
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public AnimalEntity buscarPorId(Long id) {
        return encontrarOuFalhar(id);
    }

    @Transactional
    public AnimalEntity criar(AnimalRequest request) {
        AnimalEntity entity = new AnimalEntity();
        aplicar(request, entity);
        return repository.save(entity);
    }

    @Transactional
    public AnimalEntity atualizar(Long id, AnimalRequest request) {
        AnimalEntity entity = encontrarOuFalhar(id);
        aplicar(request, entity);
        return repository.save(entity);
    }

    @Transactional
    public void remover(Long id) {
        encontrarOuFalhar(id);
        repository.deleteById(id);
    }

    private AnimalEntity encontrarOuFalhar(Long id) {
        return repository.findById(id).orElseThrow(() -> new AnimalNaoEncontradoException(id));
    }

    private void aplicar(AnimalRequest request, AnimalEntity entity) {
        ResponsavelEntity responsavel = responsavelRepository.findById(request.getResponsavelId())
                .orElseThrow(() -> new ResponsavelNaoEncontradoException(request.getResponsavelId()));
        entity.setNome(request.getNome());
        entity.setEspecie(request.getEspecie());
        entity.setRaca(request.getRaca() == null || request.getRaca().isBlank()
                ? RACA_PADRAO
                : request.getRaca());
        entity.setPorte(request.getPorte());
        entity.setSexo(request.getSexo());
        entity.setDataNascimento(request.getDataNascimento());
        entity.setPeso(request.getPeso());
        entity.setCondicaoCronica(Boolean.TRUE.equals(request.getCondicaoCronica()));
        entity.setCastrado(Boolean.TRUE.equals(request.getCastrado()));
        entity.setFoto(request.getFoto());
        entity.setAlergias(request.getAlergias());
        entity.setObservacoes(request.getObservacoes());
        entity.setResponsavel(responsavel);
    }
}
