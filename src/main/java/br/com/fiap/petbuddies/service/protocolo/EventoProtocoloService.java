package br.com.fiap.petbuddies.service.protocolo;

import br.com.fiap.petbuddies.domain.entity.EventoProtocoloEntity;
import br.com.fiap.petbuddies.domain.entity.ProtocoloEntity;
import br.com.fiap.petbuddies.domain.enums.TipoEventoProtocolo;
import br.com.fiap.petbuddies.domain.repository.EventoProtocoloRepository;
import br.com.fiap.petbuddies.domain.repository.ProtocoloRepository;
import br.com.fiap.petbuddies.dto.EventoProtocoloRequest;
import br.com.fiap.petbuddies.dto.EventoProtocoloResponse;
import br.com.fiap.petbuddies.exception.EventoProtocoloNaoEncontradoException;
import br.com.fiap.petbuddies.exception.ProtocoloNaoEncontradoException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventoProtocoloService {

    private final EventoProtocoloRepository repository;
    private final ProtocoloRepository protocoloRepository;

    public EventoProtocoloService(EventoProtocoloRepository repository, ProtocoloRepository protocoloRepository) {
        this.repository = repository;
        this.protocoloRepository = protocoloRepository;
    }

    public List<EventoProtocoloEntity> listarPorProtocolo(Long protocoloId, TipoEventoProtocolo tipo) {
        List<EventoProtocoloEntity> eventos = tipo != null
                ? repository.findByProtocoloIdAndTipo(protocoloId, tipo)
                : repository.findByProtocoloId(protocoloId);
        return eventos;
    }

    public EventoProtocoloEntity buscarPorId(Long id) {
        return encontrarOuFalhar(id);
    }

    public EventoProtocoloEntity criar(Long protocoloId, EventoProtocoloRequest request) {
        ProtocoloEntity protocolo = protocoloRepository.findById(protocoloId)
                .orElseThrow(() -> new ProtocoloNaoEncontradoException(protocoloId));
        EventoProtocoloEntity entity = new EventoProtocoloEntity();
        entity.setProtocolo(protocolo);
        aplicar(request, entity);
        return repository.save(entity);
    }

    public EventoProtocoloEntity atualizar(Long id, EventoProtocoloRequest request) {
        EventoProtocoloEntity entity = encontrarOuFalhar(id);
        aplicar(request, entity);
        return repository.save(entity);
    }

    public void remover(Long id) {
        encontrarOuFalhar(id);
        repository.deleteById(id);
    }

    private EventoProtocoloEntity encontrarOuFalhar(Long id) {
        return repository.findById(id).orElseThrow(() -> new EventoProtocoloNaoEncontradoException(id));
    }

    private void aplicar(EventoProtocoloRequest request, EventoProtocoloEntity entity) {
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
