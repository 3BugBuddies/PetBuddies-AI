package br.com.fiap.petbuddies.assembler;

import br.com.fiap.petbuddies.controller.RegraProtocoloController;
import br.com.fiap.petbuddies.controller.ProtocoloController;
import br.com.fiap.petbuddies.domain.entity.ProtocoloEntity;
import br.com.fiap.petbuddies.dto.ProtocoloResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ProtocoloModelAssembler
        implements RepresentationModelAssembler<ProtocoloEntity, EntityModel<ProtocoloResponse>> {

    @Override
    public EntityModel<ProtocoloResponse> toModel(ProtocoloEntity p) {
        return EntityModel.of(
                ProtocoloResponse.from(p),
                linkTo(methodOn(ProtocoloController.class).buscarPorId(p.getId())).withSelfRel(),
                linkTo(methodOn(RegraProtocoloController.class).listar(p.getId(), null)).withRel("eventos"),
                linkTo(methodOn(ProtocoloController.class).listar()).withRel("protocolos"));
    }
}
