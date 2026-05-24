package br.com.fiap.petbuddies.service.protocolo;

import br.com.fiap.petbuddies.domain.entity.ProtocoloEntity;
import br.com.fiap.petbuddies.domain.enums.CategoriaProtocolo;
import br.com.fiap.petbuddies.domain.enums.Especie;
import br.com.fiap.petbuddies.domain.repository.ProtocoloRepository;
import br.com.fiap.petbuddies.dto.protocolo.ProtocoloRequest;
import br.com.fiap.petbuddies.dto.protocolo.ProtocoloResponse;
import br.com.fiap.petbuddies.exception.ProtocoloNaoEncontradoException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProtocoloService {

    private final ProtocoloRepository repository;

    public ProtocoloService(ProtocoloRepository repository) {
        this.repository = repository;
    }

    public List<ProtocoloResponse> listarAtivos() {
        return repository.findByAtivoTrue().stream().map(ProtocoloResponse::from).toList();
    }

    public List<ProtocoloResponse> buscar(CategoriaProtocolo categoria, Especie especie) {
        if (categoria != null && especie != null) {
            return repository.findByCategoriaAndEspecieAndAtivoTrue(categoria, especie)
                    .stream().map(ProtocoloResponse::from).toList();
        }
        if (categoria != null) {
            return repository.findByCategoriaAndAtivoTrue(categoria)
                    .stream().map(ProtocoloResponse::from).toList();
        }
        return listarAtivos();
    }

    public ProtocoloResponse buscarPorId(Long id) {
        return ProtocoloResponse.from(encontrarOuFalhar(id));
    }

    public ProtocoloResponse criar(ProtocoloRequest request) {
        ProtocoloEntity entity = new ProtocoloEntity();
        aplicar(request, entity);
        return ProtocoloResponse.from(repository.save(entity));
    }

    public ProtocoloResponse atualizar(Long id, ProtocoloRequest request) {
        ProtocoloEntity entity = encontrarOuFalhar(id);
        aplicar(request, entity);
        return ProtocoloResponse.from(repository.save(entity));
    }

    public void remover(Long id) {
        encontrarOuFalhar(id);
        repository.deleteById(id);
    }

    private ProtocoloEntity encontrarOuFalhar(Long id) {
        return repository.findById(id).orElseThrow(() -> new ProtocoloNaoEncontradoException(id));
    }

    private void aplicar(ProtocoloRequest request, ProtocoloEntity entity) {
        entity.setNome(request.getNome());
        entity.setCategoria(request.getCategoria());
        entity.setEspecie(request.getEspecie());
        entity.setPorte(request.getPorte());
        entity.setSexo(request.getSexo());
        entity.setCastrado(request.getCastrado());
        if (request.getAtivo() != null) entity.setAtivo(request.getAtivo());
        entity.setIdadeMinMeses(request.getIdadeMinMeses());
        entity.setIdadeMaxMeses(request.getIdadeMaxMeses());
        entity.setDescricao(request.getDescricao());
    }
}
