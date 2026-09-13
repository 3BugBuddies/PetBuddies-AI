package br.com.fiap.petbuddies.service.cadastro;

import br.com.fiap.petbuddies.domain.embeddable.Contato;
import br.com.fiap.petbuddies.domain.entity.ResponsavelEntity;
import br.com.fiap.petbuddies.domain.repository.AnimalRepository;
import br.com.fiap.petbuddies.domain.repository.ResponsavelRepository;
import br.com.fiap.petbuddies.domain.repository.UsuarioRepository;
import br.com.fiap.petbuddies.dto.cadastro.ResponsavelRequest;
import br.com.fiap.petbuddies.exception.cadastro.ResponsavelComVinculosException;
import br.com.fiap.petbuddies.exception.cadastro.ResponsavelNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ResponsavelService {

    private final ResponsavelRepository repository;
    private final AnimalRepository animalRepository;
    private final UsuarioRepository usuarioRepository;

    public ResponsavelService(
            ResponsavelRepository repository, AnimalRepository animalRepository, UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.animalRepository = animalRepository;
        this.usuarioRepository = usuarioRepository;
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
        if (animalRepository.existsByResponsavelId(id)) {
            throw new ResponsavelComVinculosException(id, "animais");
        }
        if (usuarioRepository.existsByResponsavelId(id)) {
            throw new ResponsavelComVinculosException(id, "usuário de acesso");
        }
        repository.deleteById(id);
    }

    private ResponsavelEntity encontrarOuFalhar(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResponsavelNaoEncontradoException(id));
    }

    private void aplicar(ResponsavelRequest request, ResponsavelEntity entity) {
        entity.setNome(request.getNome());
        entity.setContato(new Contato(request.getTelefone(), request.getEmail()));
    }
}
