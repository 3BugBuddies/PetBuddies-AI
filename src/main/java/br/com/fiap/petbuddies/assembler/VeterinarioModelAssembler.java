package br.com.fiap.petbuddies.assembler;

import br.com.fiap.petbuddies.controller.cadastro.ClinicaController;
import br.com.fiap.petbuddies.controller.cadastro.VeterinarioController;
import br.com.fiap.petbuddies.domain.entity.VeterinarioEntity;
import br.com.fiap.petbuddies.dto.cadastro.VeterinarioResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class VeterinarioModelAssembler
        implements RepresentationModelAssembler<VeterinarioEntity, EntityModel<VeterinarioResponse>> {

    @Override
    public EntityModel<VeterinarioResponse> toModel(VeterinarioEntity v) {
        VeterinarioResponse dto = VeterinarioResponse.from(v);
        return EntityModel.of(
                dto,
                linkTo(methodOn(VeterinarioController.class).buscarPorId(v.getId())).withSelfRel(),
                linkTo(methodOn(VeterinarioController.class).listar(null)).withRel("veterinarios"),
                linkTo(methodOn(ClinicaController.class).buscarPorId(dto.getClinicaId())).withRel("clinica"));
    }
}
