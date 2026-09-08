package br.com.fiap.petbuddies.hateoas;

import br.com.fiap.petbuddies.controller.protocolo.EventoProtocoloController;
import br.com.fiap.petbuddies.controller.protocolo.ProtocoloController;
import br.com.fiap.petbuddies.dto.protocolo.EventoProtocoloResponse;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class EventoProtocoloAssembler {

    public EntityModel<EventoProtocoloResponse> toModel(EventoProtocoloResponse evento) {
        EntityModel<EventoProtocoloResponse> model = EntityModel.of(evento,
                linkTo(methodOn(EventoProtocoloController.class).buscarPorId(evento.getId())).withSelfRel());

        if (evento.getProtocoloId() != null) {
            model.add(linkTo(methodOn(ProtocoloController.class)
                    .buscarPorId(evento.getProtocoloId())).withRel("protocolo"));
        }
        return model;
    }

    public CollectionModel<EntityModel<EventoProtocoloResponse>> toCollectionModel(
            Long protocoloId, List<EventoProtocoloResponse> eventos) {
        List<EntityModel<EventoProtocoloResponse>> itens = eventos.stream().map(this::toModel).toList();
        return CollectionModel.of(itens,
                linkTo(methodOn(EventoProtocoloController.class).listar(protocoloId, null)).withSelfRel(),
                linkTo(methodOn(ProtocoloController.class).buscarPorId(protocoloId)).withRel("protocolo"));
    }
}
