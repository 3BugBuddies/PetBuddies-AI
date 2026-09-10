package br.com.fiap.petbuddies.assembler;

import br.com.fiap.petbuddies.controller.cadastro.AnimalController;
import br.com.fiap.petbuddies.controller.atendimento.ProcedimentoController;
import br.com.fiap.petbuddies.controller.atendimento.RegistroAtendimentoController;
import br.com.fiap.petbuddies.controller.cadastro.VeterinarioController;
import br.com.fiap.petbuddies.domain.entity.ProcedimentoEntity;
import br.com.fiap.petbuddies.dto.atendimento.ProcedimentoResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ProcedimentoModelAssembler
        implements RepresentationModelAssembler<ProcedimentoEntity, EntityModel<ProcedimentoResponse>> {

    @Override
    public EntityModel<ProcedimentoResponse> toModel(ProcedimentoEntity p) {
        ProcedimentoResponse dto = ProcedimentoResponse.from(p);
        return EntityModel.of(
                dto,
                linkTo(methodOn(ProcedimentoController.class).buscarPorId(p.getId())).withSelfRel(),
                linkTo(methodOn(ProcedimentoController.class).listar(null, null)).withRel("procedimentos"),
                linkTo(methodOn(RegistroAtendimentoController.class).buscarPorId(dto.getRegistroAtendimentoId())).withRel("registro-atendimento"),
                linkTo(methodOn(AnimalController.class).buscarPorId(dto.getAnimalId())).withRel("animal"),
                linkTo(methodOn(VeterinarioController.class).buscarPorId(dto.getVeterinarioId())).withRel("veterinario"));
    }
}
