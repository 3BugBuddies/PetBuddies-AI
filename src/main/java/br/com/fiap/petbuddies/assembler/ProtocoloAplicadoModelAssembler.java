package br.com.fiap.petbuddies.assembler;

import br.com.fiap.petbuddies.controller.MotorPlanoController;
import br.com.fiap.petbuddies.domain.entity.PlanoCuidadoEntity;
import br.com.fiap.petbuddies.dto.cuidado.ProtocoloAplicadoResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ProtocoloAplicadoModelAssembler
        implements RepresentationModelAssembler<PlanoCuidadoEntity, EntityModel<ProtocoloAplicadoResponse>> {

    @Override
    public EntityModel<ProtocoloAplicadoResponse> toModel(PlanoCuidadoEntity plano) {
        ProtocoloAplicadoResponse dto = ProtocoloAplicadoResponse.from(plano);
        return EntityModel.of(dto,
                linkTo(methodOn(MotorPlanoController.class).protocoloAplicado(dto.getAnimalId())).withSelfRel(),
                linkTo(methodOn(MotorPlanoController.class).buscarPlano(dto.getAnimalId())).withRel("plano"),
                linkTo(methodOn(MotorPlanoController.class)
                        .listarEventos(dto.getAnimalId(), null)).withRel("eventos"));
    }
}
