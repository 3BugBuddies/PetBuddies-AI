package br.com.fiap.petbuddies.assembler;

import br.com.fiap.petbuddies.controller.RegraProtocoloController;
import br.com.fiap.petbuddies.controller.ProtocoloController;
import br.com.fiap.petbuddies.domain.entity.RegraProtocoloEntity;
import br.com.fiap.petbuddies.dto.RegraProtocoloResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class RegraProtocoloModelAssembler
        implements RepresentationModelAssembler<RegraProtocoloEntity, EntityModel<RegraProtocoloResponse>> {

    @Override
    public EntityModel<RegraProtocoloResponse> toModel(RegraProtocoloEntity e) {
        Long protocoloId = e.getProtocolo() != null ? e.getProtocolo().getId() : null;

        EntityModel<RegraProtocoloResponse> model = EntityModel.of(
                RegraProtocoloResponse.from(e),
                linkTo(methodOn(RegraProtocoloController.class).buscarPorId(e.getId())).withSelfRel());

        if (protocoloId != null) {
            model.add(linkTo(methodOn(RegraProtocoloController.class)
                    .listar(protocoloId, null)).withRel("eventos"));
            model.add(linkTo(methodOn(ProtocoloController.class)
                    .buscarPorId(protocoloId)).withRel("protocolo"));
        }
        return model;
    }
}
