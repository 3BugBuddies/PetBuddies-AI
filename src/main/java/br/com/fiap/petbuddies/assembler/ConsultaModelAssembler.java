package br.com.fiap.petbuddies.assembler;

import br.com.fiap.petbuddies.controller.cadastro.AnimalController;
import br.com.fiap.petbuddies.controller.atendimento.ConsultaController;
import br.com.fiap.petbuddies.controller.cadastro.VeterinarioController;
import br.com.fiap.petbuddies.domain.entity.ConsultaEntity;
import br.com.fiap.petbuddies.dto.atendimento.ConsultaResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ConsultaModelAssembler
        implements RepresentationModelAssembler<ConsultaEntity, EntityModel<ConsultaResponse>> {

    @Override
    public EntityModel<ConsultaResponse> toModel(ConsultaEntity c) {
        ConsultaResponse dto = ConsultaResponse.from(c);
        return EntityModel.of(
                dto,
                linkTo(methodOn(ConsultaController.class).buscarPorId(c.getId())).withSelfRel(),
                linkTo(methodOn(ConsultaController.class).listar(null, null)).withRel("consultas"),
                linkTo(methodOn(AnimalController.class).buscarPorId(dto.getAnimalId())).withRel("animal"),
                linkTo(methodOn(VeterinarioController.class).buscarPorId(dto.getVeterinarioId())).withRel("veterinario"));
    }
}
