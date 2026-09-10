package br.com.fiap.petbuddies.assembler;

import br.com.fiap.petbuddies.controller.cadastro.ClinicaController;
import br.com.fiap.petbuddies.controller.atendimento.CondicaoClinicaController;
import br.com.fiap.petbuddies.controller.cadastro.VeterinarioController;
import br.com.fiap.petbuddies.domain.entity.CondicaoClinicaEntity;
import br.com.fiap.petbuddies.dto.atendimento.CondicaoClinicaResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CondicaoClinicaModelAssembler
        implements RepresentationModelAssembler<CondicaoClinicaEntity, EntityModel<CondicaoClinicaResponse>> {

    @Override
    public EntityModel<CondicaoClinicaResponse> toModel(CondicaoClinicaEntity c) {
        CondicaoClinicaResponse dto = CondicaoClinicaResponse.from(c);
        return EntityModel.of(
                dto,
                linkTo(methodOn(CondicaoClinicaController.class).buscarPorId(c.getId())).withSelfRel(),
                linkTo(methodOn(CondicaoClinicaController.class).listar(null)).withRel("condicoes-clinicas"),
                linkTo(methodOn(ClinicaController.class).buscarPorId(dto.getClinicaId())).withRel("clinica"),
                linkTo(methodOn(VeterinarioController.class).buscarPorId(dto.getVeterinarioAutorId())).withRel("autor"));
    }
}
