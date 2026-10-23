package br.com.fiap.petbuddies.service.cadastro;

import br.com.fiap.petbuddies.domain.embeddable.Contato;
import br.com.fiap.petbuddies.domain.entity.AnimalEntity;
import br.com.fiap.petbuddies.domain.entity.ResponsavelEntity;
import br.com.fiap.petbuddies.domain.repository.AnimalRepository;
import br.com.fiap.petbuddies.domain.repository.ResponsavelRepository;
import br.com.fiap.petbuddies.domain.repository.UsuarioRepository;
import br.com.fiap.petbuddies.dto.cadastro.ResponsavelRequest;
import br.com.fiap.petbuddies.dto.cadastro.ResponsavelResumoDto;
import br.com.fiap.petbuddies.exception.cadastro.ResponsavelComVinculosException;
import br.com.fiap.petbuddies.exception.cadastro.ResponsavelNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    /** A lista do painel: busca por nome, e-mail ou telefone, com os animais e o acesso ao app de cada um. */
    @Transactional(readOnly = true)
    public List<ResponsavelResumoDto> listarResumos(String busca) {
        List<ResponsavelEntity> responsaveis = buscarPorTexto(busca);
        if (responsaveis.isEmpty()) {
            return List.of();
        }
        List<Long> ids = responsaveis.stream().map(ResponsavelEntity::getId).toList();
        Map<Long, List<String>> animais = animalRepository.findPorResponsaveis(ids).stream()
                .collect(Collectors.groupingBy(a -> a.getResponsavel().getId(),
                        Collectors.mapping(AnimalEntity::getNome, Collectors.toList())));
        Set<Long> comAcesso = new HashSet<>(usuarioRepository.findResponsaveisComAcessoAtivo(ids));
        return responsaveis.stream()
                .map(r -> new ResponsavelResumoDto(r.getId(), r.getNome(), r.getContato().getTelefone(),
                        r.getContato().getEmail(), animais.getOrDefault(r.getId(), List.of()), comAcesso.contains(r.getId())))
                .toList();
    }

    private List<ResponsavelEntity> buscarPorTexto(String busca) {
        if (busca == null || busca.isBlank()) {
            return repository.findAllByOrderByNomeAsc();
        }
        String texto = "%" + busca.trim().toLowerCase() + "%";
        String digitos = busca.replaceAll("\\D", "");
        return digitos.isEmpty() ? repository.buscarPorTexto(texto) : repository.buscarPorTextoOuTelefone(texto, "%" + digitos + "%");
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
        entity.setEndereco(request.getEndereco());
    }
}
