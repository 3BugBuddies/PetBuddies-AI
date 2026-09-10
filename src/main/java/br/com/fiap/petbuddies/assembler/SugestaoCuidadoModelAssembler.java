package br.com.fiap.petbuddies.assembler;

import br.com.fiap.petbuddies.controller.cuidado.MotorPlanoController;
import br.com.fiap.petbuddies.dto.cuidado.SugestaoCuidadoDto;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Recebe o DTO, não uma entidade — igual ao {@code PlanoModelAssembler}. Uma
 * sugestão de motivo {@code RECORRENCIA_DEVIDA} ou {@code NUNCA_REALIZADO} não
 * tem entidade nenhuma por trás: é uma regra do catálogo sem item materializado.
 */
@Component
public class SugestaoCuidadoModelAssembler
        implements RepresentationModelAssembler<SugestaoCuidadoDto, EntityModel<SugestaoCuidadoDto>> {

    @Override
    public EntityModel<SugestaoCuidadoDto> toModel(SugestaoCuidadoDto sugestao) {
        return EntityModel.of(sugestao,
                linkTo(methodOn(MotorPlanoController.class).sugestoes(sugestao.getAnimalId())).withSelfRel(),
                linkTo(methodOn(MotorPlanoController.class).buscarPlano(sugestao.getAnimalId())).withRel("plano"));
    }
}
