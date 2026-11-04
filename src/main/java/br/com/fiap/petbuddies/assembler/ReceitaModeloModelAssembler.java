package br.com.fiap.petbuddies.assembler;

import br.com.fiap.petbuddies.controller.cadastro.ClinicaController;
import br.com.fiap.petbuddies.controller.cadastro.VeterinarioController;
import br.com.fiap.petbuddies.controller.prescricao.ReceitaModeloController;
import br.com.fiap.petbuddies.domain.entity.ReceitaModeloEntity;
import br.com.fiap.petbuddies.dto.prescricao.ReceitaModeloResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ReceitaModeloModelAssembler
        implements RepresentationModelAssembler<ReceitaModeloEntity, EntityModel<ReceitaModeloResponse>> {

    @Override
    public EntityModel<ReceitaModeloResponse> toModel(ReceitaModeloEntity r) {
        ReceitaModeloResponse dto = ReceitaModeloResponse.from(r);
        return EntityModel.of(
                dto,
                linkTo(methodOn(ReceitaModeloController.class).buscarPorId(r.getId(), null)).withSelfRel(),
                linkTo(methodOn(ReceitaModeloController.class).listar(null)).withRel("receitas-modelo"),
                linkTo(methodOn(ClinicaController.class).buscarPorId(dto.getClinicaId())).withRel("clinica"),
                linkTo(methodOn(VeterinarioController.class).buscarPorId(dto.getAutorId())).withRel("autor"));
    }
}
