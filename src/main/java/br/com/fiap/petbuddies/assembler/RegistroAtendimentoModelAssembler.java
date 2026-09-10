package br.com.fiap.petbuddies.assembler;

import br.com.fiap.petbuddies.controller.cadastro.AnimalController;
import br.com.fiap.petbuddies.controller.atendimento.ConsultaController;
import br.com.fiap.petbuddies.controller.atendimento.RegistroAtendimentoController;
import br.com.fiap.petbuddies.domain.entity.RegistroAtendimentoEntity;
import br.com.fiap.petbuddies.dto.atendimento.RegistroAtendimentoResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class RegistroAtendimentoModelAssembler
        implements RepresentationModelAssembler<RegistroAtendimentoEntity, EntityModel<RegistroAtendimentoResponse>> {

    @Override
    public EntityModel<RegistroAtendimentoResponse> toModel(RegistroAtendimentoEntity r) {
        RegistroAtendimentoResponse dto = RegistroAtendimentoResponse.from(r);
        return EntityModel.of(
                dto,
                linkTo(methodOn(RegistroAtendimentoController.class).buscarPorId(r.getId())).withSelfRel(),
                linkTo(methodOn(RegistroAtendimentoController.class).listar(null, null)).withRel("registros-atendimento"),
                linkTo(methodOn(AnimalController.class).buscarPorId(dto.getAnimalId())).withRel("animal"),
                linkTo(methodOn(ConsultaController.class).buscarPorId(dto.getConsultaId())).withRel("consulta"));
    }
}
