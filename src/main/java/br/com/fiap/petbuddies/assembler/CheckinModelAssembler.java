package br.com.fiap.petbuddies.assembler;

import br.com.fiap.petbuddies.controller.AnimalController;
import br.com.fiap.petbuddies.controller.CheckinController;
import br.com.fiap.petbuddies.dto.CheckinResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CheckinModelAssembler
        implements RepresentationModelAssembler<CheckinResponse, EntityModel<CheckinResponse>> {

    @Override
    public EntityModel<CheckinResponse> toModel(CheckinResponse c) {
        return EntityModel.of(
                c,
                linkTo(methodOn(CheckinController.class).buscarPorId(c.getId())).withSelfRel(),
                linkTo(methodOn(CheckinController.class).listar(c.getAnimalId())).withRel("checkins"),
                linkTo(methodOn(AnimalController.class).buscarPorId(c.getAnimalId())).withRel("animal"));
    }
}
