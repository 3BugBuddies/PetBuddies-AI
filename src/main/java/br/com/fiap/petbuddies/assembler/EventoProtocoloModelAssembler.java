package br.com.fiap.petbuddies.assembler;

import br.com.fiap.petbuddies.controller.protocolo.EventoProtocoloController;
import br.com.fiap.petbuddies.controller.protocolo.ProtocoloController;
import br.com.fiap.petbuddies.domain.entity.EventoProtocoloEntity;
import br.com.fiap.petbuddies.dto.protocolo.EventoProtocoloResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class EventoProtocoloModelAssembler
        implements RepresentationModelAssembler<EventoProtocoloEntity, EntityModel<EventoProtocoloResponse>> {

    @Override
    public EntityModel<EventoProtocoloResponse> toModel(EventoProtocoloEntity e) {
        Long protocoloId = e.getProtocolo() != null ? e.getProtocolo().getId() : null;

        EntityModel<EventoProtocoloResponse> model = EntityModel.of(
                EventoProtocoloResponse.from(e),
                linkTo(methodOn(EventoProtocoloController.class).buscarPorId(e.getId())).withSelfRel());

        if (protocoloId != null) {
            model.add(linkTo(methodOn(EventoProtocoloController.class)
                    .listar(protocoloId, null)).withRel("eventos"));
            model.add(linkTo(methodOn(ProtocoloController.class)
                    .buscarPorId(protocoloId)).withRel("protocolo"));
        }
        return model;
    }
}
