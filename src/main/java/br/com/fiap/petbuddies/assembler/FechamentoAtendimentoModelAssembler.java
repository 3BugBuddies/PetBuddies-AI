package br.com.fiap.petbuddies.assembler;

import br.com.fiap.petbuddies.controller.atendimento.ConsultaController;
import br.com.fiap.petbuddies.controller.atendimento.RegistroAtendimentoController;
import br.com.fiap.petbuddies.dto.atendimento.FechamentoAtendimentoResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Recebe o response, não a entidade — como o {@code PlanoModelAssembler}: o
 * fechamento não tem uma linha própria no banco, é a soma de várias.
 */
@Component
public class FechamentoAtendimentoModelAssembler
        implements RepresentationModelAssembler<FechamentoAtendimentoResponse, EntityModel<FechamentoAtendimentoResponse>> {

    @Override
    public EntityModel<FechamentoAtendimentoResponse> toModel(FechamentoAtendimentoResponse r) {
        return EntityModel.of(
                r,
                linkTo(methodOn(ConsultaController.class).buscarPorId(r.getConsultaId())).withSelfRel(),
                linkTo(methodOn(RegistroAtendimentoController.class)
                        .buscarPorId(r.getRegistroAtendimento().getId())).withRel("registro-atendimento"));
    }
}
