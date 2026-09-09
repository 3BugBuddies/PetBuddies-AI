package br.com.fiap.petbuddies.service;

import br.com.fiap.petbuddies.domain.entity.ProtocoloEntity;
import br.com.fiap.petbuddies.domain.enums.CategoriaProtocolo;
import br.com.fiap.petbuddies.domain.enums.Especie;
import br.com.fiap.petbuddies.domain.repository.ProtocoloRepository;
import br.com.fiap.petbuddies.dto.ProtocoloRequest;
import br.com.fiap.petbuddies.dto.ProtocoloResponse;
import br.com.fiap.petbuddies.exception.ProtocoloNaoEncontradoException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProtocoloService {

    private final ProtocoloRepository repository;

    public ProtocoloService(ProtocoloRepository repository) {
        this.repository = repository;
    }

    public List<ProtocoloEntity> listarAtivos() {
        return repository.findByAtivoTrue();
    }

    public List<ProtocoloEntity> buscar(CategoriaProtocolo categoria, Especie especie) {
        if (categoria != null && especie != null) {
            return repository.findByCategoriaAndEspecieAndAtivoTrue(categoria, especie);
        }
        if (categoria != null) {
            return repository.findByCategoriaAndAtivoTrue(categoria);
        }
        return listarAtivos();
    }

    public ProtocoloEntity buscarPorId(Long id) {
        return encontrarOuFalhar(id);
    }

    public ProtocoloEntity criar(ProtocoloRequest request) {
        ProtocoloEntity entity = new ProtocoloEntity();
        aplicar(request, entity);
        return repository.save(entity);
    }

    public ProtocoloEntity atualizar(Long id, ProtocoloRequest request) {
        ProtocoloEntity entity = encontrarOuFalhar(id);
        aplicar(request, entity);
        return repository.save(entity);
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
        if (request.getAtivo() != null) entity.setAtivo(request.getAtivo());
        entity.setDescricao(request.getDescricao());
    }
}
