package br.com.fiap.petbuddies.hateoas;

import br.com.fiap.petbuddies.controller.motor.MotorPlanoController;
import br.com.fiap.petbuddies.dto.motor.PlanoResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Envelopa o plano de cuidado. O link de eventos e o que o checklist do tutor
 * segue para paginar os itens do plano.
 */
@Component
public class PlanoAssembler {

    public EntityModel<PlanoResponse> toModel(Long animalId, PlanoResponse plano) {
        return EntityModel.of(plano,
                linkTo(methodOn(MotorPlanoController.class).buscarPlano(animalId)).withSelfRel(),
                linkTo(methodOn(MotorPlanoController.class)
                        .listarEventos(animalId, null)).withRel("eventos"));
    }
}
