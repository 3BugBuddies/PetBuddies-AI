package br.com.fiap.petbuddies.assembler;

import br.com.fiap.petbuddies.controller.cadastro.ResponsavelController;
import br.com.fiap.petbuddies.domain.entity.ResponsavelEntity;
import br.com.fiap.petbuddies.dto.cadastro.ResponsavelResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ResponsavelModelAssembler
        implements RepresentationModelAssembler<ResponsavelEntity, EntityModel<ResponsavelResponse>> {

    @Override
    public EntityModel<ResponsavelResponse> toModel(ResponsavelEntity r) {
        return EntityModel.of(
                ResponsavelResponse.from(r),
                linkTo(methodOn(ResponsavelController.class).buscarPorId(r.getId())).withSelfRel(),
                linkTo(methodOn(ResponsavelController.class).listar(null)).withRel("responsaveis"));
    }
}
