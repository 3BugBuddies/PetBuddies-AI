package br.com.fiap.petbuddies.assembler;

import br.com.fiap.petbuddies.controller.CondicaoClinicaController;
import br.com.fiap.petbuddies.controller.PrescricaoController;
import br.com.fiap.petbuddies.controller.RegraPrescricaoController;
import br.com.fiap.petbuddies.domain.entity.RegraPrescricaoEntity;
import br.com.fiap.petbuddies.dto.RegraPrescricaoResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class RegraPrescricaoModelAssembler
        implements RepresentationModelAssembler<RegraPrescricaoEntity, EntityModel<RegraPrescricaoResponse>> {

    @Override
    public EntityModel<RegraPrescricaoResponse> toModel(RegraPrescricaoEntity r) {
        RegraPrescricaoResponse dto = RegraPrescricaoResponse.from(r);
        return EntityModel.of(
                dto,
                linkTo(methodOn(RegraPrescricaoController.class).buscarPorId(r.getId())).withSelfRel(),
                linkTo(methodOn(RegraPrescricaoController.class).listar(null)).withRel("regras-prescricao"),
                linkTo(methodOn(PrescricaoController.class).buscarPorId(dto.getPrescricaoId())).withRel("prescricao"),
                linkTo(methodOn(CondicaoClinicaController.class).buscarPorId(dto.getCondicaoClinicaId())).withRel("condicao-clinica"));
    }
}
