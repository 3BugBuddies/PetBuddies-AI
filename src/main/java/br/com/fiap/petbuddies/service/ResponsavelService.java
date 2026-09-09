package br.com.fiap.petbuddies.service;

import br.com.fiap.petbuddies.domain.entity.ResponsavelEntity;
import br.com.fiap.petbuddies.domain.repository.ResponsavelRepository;
import br.com.fiap.petbuddies.dto.ResponsavelRequest;
import br.com.fiap.petbuddies.exception.ResponsavelNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ResponsavelService {

    private final ResponsavelRepository repository;

    public ResponsavelService(ResponsavelRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ResponsavelEntity> listar(String nome) {
        if (nome != null && !nome.isBlank()) {
            return repository.findByNomeContainingIgnoreCase(nome);
        }
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public ResponsavelEntity buscarPorId(Long id) {
        return encontrarOuFalhar(id);
    }

    @Transactional
    public ResponsavelEntity criar(ResponsavelRequest request) {
        ResponsavelEntity entity = new ResponsavelEntity();
        aplicar(request, entity);
        return repository.save(entity);
    }

    @Transactional
    public ResponsavelEntity atualizar(Long id, ResponsavelRequest request) {
        ResponsavelEntity entity = encontrarOuFalhar(id);
        aplicar(request, entity);
        return repository.save(entity);
    }

    @Transactional
    public void remover(Long id) {
        encontrarOuFalhar(id);
        repository.deleteById(id);
    }

    private ResponsavelEntity encontrarOuFalhar(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResponsavelNaoEncontradoException(id));
    }

    private void aplicar(ResponsavelRequest request, ResponsavelEntity entity) {
        entity.setNome(request.getNome());
        entity.setTelefone(request.getTelefone());
        entity.setEmail(request.getEmail());
    }
}
