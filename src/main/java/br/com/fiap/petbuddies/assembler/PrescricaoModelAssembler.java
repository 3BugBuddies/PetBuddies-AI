package br.com.fiap.petbuddies.assembler;

import br.com.fiap.petbuddies.controller.cadastro.AnimalController;
import br.com.fiap.petbuddies.controller.prescricao.PrescricaoController;
import br.com.fiap.petbuddies.controller.atendimento.RegistroAtendimentoController;
import br.com.fiap.petbuddies.controller.cadastro.VeterinarioController;
import br.com.fiap.petbuddies.domain.entity.PrescricaoEntity;
import br.com.fiap.petbuddies.dto.prescricao.PrescricaoResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PrescricaoModelAssembler
        implements RepresentationModelAssembler<PrescricaoEntity, EntityModel<PrescricaoResponse>> {

    @Override
    public EntityModel<PrescricaoResponse> toModel(PrescricaoEntity p) {
        PrescricaoResponse dto = PrescricaoResponse.from(p);
        return EntityModel.of(
                dto,
                linkTo(methodOn(PrescricaoController.class).buscarPorId(p.getId())).withSelfRel(),
                linkTo(methodOn(PrescricaoController.class).listar(null, null)).withRel("prescricoes"),
                linkTo(methodOn(RegistroAtendimentoController.class).buscarPorId(dto.getRegistroAtendimentoId())).withRel("registro-atendimento"),
                linkTo(methodOn(AnimalController.class).buscarPorId(dto.getAnimalId())).withRel("animal"),
                linkTo(methodOn(VeterinarioController.class).buscarPorId(dto.getVeterinarioId())).withRel("veterinario"));
    }
}
