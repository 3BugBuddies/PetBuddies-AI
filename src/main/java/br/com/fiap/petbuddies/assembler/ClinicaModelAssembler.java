package br.com.fiap.petbuddies.assembler;

import br.com.fiap.petbuddies.controller.ClinicaController;
import br.com.fiap.petbuddies.domain.entity.ClinicaEntity;
import br.com.fiap.petbuddies.dto.ClinicaResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ClinicaModelAssembler
        implements RepresentationModelAssembler<ClinicaEntity, EntityModel<ClinicaResponse>> {

    @Override
    public EntityModel<ClinicaResponse> toModel(ClinicaEntity c) {
        return EntityModel.of(
                ClinicaResponse.from(c),
                linkTo(methodOn(ClinicaController.class).buscarPorId(c.getId())).withSelfRel(),
                linkTo(methodOn(ClinicaController.class).listar()).withRel("clinicas"));
    }
}
