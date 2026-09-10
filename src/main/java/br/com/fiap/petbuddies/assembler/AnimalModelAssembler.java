package br.com.fiap.petbuddies.assembler;

import br.com.fiap.petbuddies.controller.cadastro.AnimalController;
import br.com.fiap.petbuddies.controller.cadastro.ResponsavelController;
import br.com.fiap.petbuddies.domain.entity.AnimalEntity;
import br.com.fiap.petbuddies.dto.cadastro.AnimalResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class AnimalModelAssembler
        implements RepresentationModelAssembler<AnimalEntity, EntityModel<AnimalResponse>> {

    @Override
    public EntityModel<AnimalResponse> toModel(AnimalEntity a) {
        AnimalResponse dto = AnimalResponse.from(a);
        return EntityModel.of(
                dto,
                linkTo(methodOn(AnimalController.class).buscarPorId(a.getId())).withSelfRel(),
                linkTo(methodOn(AnimalController.class).listar(null, null)).withRel("animais"),
                linkTo(methodOn(ResponsavelController.class).buscarPorId(dto.getResponsavelId())).withRel("responsavel"));
    }
}
