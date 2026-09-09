package br.com.fiap.petbuddies.service;

import br.com.fiap.petbuddies.domain.entity.RegraProtocoloEntity;
import br.com.fiap.petbuddies.domain.entity.ProtocoloEntity;
import br.com.fiap.petbuddies.domain.enums.TipoCuidado;
import br.com.fiap.petbuddies.domain.repository.RegraProtocoloRepository;
import br.com.fiap.petbuddies.domain.repository.ProtocoloRepository;
import br.com.fiap.petbuddies.dto.RegraProtocoloRequest;
import br.com.fiap.petbuddies.dto.RegraProtocoloResponse;
import br.com.fiap.petbuddies.exception.RegraProtocoloNaoEncontradoException;
import br.com.fiap.petbuddies.exception.ProtocoloNaoEncontradoException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegraProtocoloService {

    private final RegraProtocoloRepository repository;
    private final ProtocoloRepository protocoloRepository;

    public RegraProtocoloService(RegraProtocoloRepository repository, ProtocoloRepository protocoloRepository) {
        this.repository = repository;
        this.protocoloRepository = protocoloRepository;
    }

    public List<RegraProtocoloEntity> listarPorProtocolo(Long protocoloId, TipoCuidado tipo) {
        List<RegraProtocoloEntity> eventos = tipo != null
                ? repository.findByProtocoloIdAndTipo(protocoloId, tipo)
                : repository.findByProtocoloId(protocoloId);
        return eventos;
    }

    public RegraProtocoloEntity buscarPorId(Long id) {
        return encontrarOuFalhar(id);
    }

    public RegraProtocoloEntity criar(Long protocoloId, RegraProtocoloRequest request) {
        ProtocoloEntity protocolo = protocoloRepository.findById(protocoloId)
                .orElseThrow(() -> new ProtocoloNaoEncontradoException(protocoloId));
        RegraProtocoloEntity entity = new RegraProtocoloEntity();
        entity.setProtocolo(protocolo);
        aplicar(request, entity);
        return repository.save(entity);
    }

    public RegraProtocoloEntity atualizar(Long id, RegraProtocoloRequest request) {
        RegraProtocoloEntity entity = encontrarOuFalhar(id);
        aplicar(request, entity);
        return repository.save(entity);
    }

    public void remover(Long id) {
        encontrarOuFalhar(id);
        repository.deleteById(id);
    }

    private RegraProtocoloEntity encontrarOuFalhar(Long id) {
        return repository.findById(id).orElseThrow(() -> new RegraProtocoloNaoEncontradoException(id));
    }

    private void aplicar(RegraProtocoloRequest request, RegraProtocoloEntity entity) {
        entity.setTipo(request.getTipo());
        entity.setNome(request.getNome());
        entity.setOffset(request.getOffset());
        entity.setUnidadeOffset(request.getUnidadeOffset());
        entity.setAncora(request.getAncora());
        entity.setIntervalo(request.getIntervalo());
        entity.setUnidadeIntervalo(request.getUnidadeIntervalo());
        entity.setRepeticoes(request.getRepeticoes());
        entity.setDescricao(request.getDescricao());
    }
}
