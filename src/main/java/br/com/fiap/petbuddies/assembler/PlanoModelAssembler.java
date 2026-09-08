package br.com.fiap.petbuddies.assembler;

import br.com.fiap.petbuddies.controller.motor.MotorPlanoController;
import br.com.fiap.petbuddies.dto.motor.PlanoResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Diferente dos outros dois, este assembler recebe o <b>response</b> e nao a
 * entidade.
 *
 * <p>Nao e desleixo: {@code PlanoResponse} carrega dois campos que nao existem no
 * plano — {@code criado} e {@code motivo}, que dizem se a instanciacao criou um
 * plano novo ou devolveu o que ja existia. Reconstruir isso a partir da entidade
 * exigiria passar os dois como parametro, e o assembler deixaria de ter a
 * assinatura da interface do Spring assim mesmo.</p>
 */
@Component
public class PlanoModelAssembler
        implements RepresentationModelAssembler<PlanoResponse, EntityModel<PlanoResponse>> {

    @Override
    public EntityModel<PlanoResponse> toModel(PlanoResponse plano) {
        return EntityModel.of(plano,
                linkTo(methodOn(MotorPlanoController.class).buscarPlano(plano.getAnimalId())).withSelfRel(),
                linkTo(methodOn(MotorPlanoController.class)
                        .listarEventos(plano.getAnimalId(), null)).withRel("eventos"));
    }
}
