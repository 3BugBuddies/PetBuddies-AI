package br.com.fiap.petbuddies.assembler;

import br.com.fiap.petbuddies.controller.atendimento.ConsultaController;
import br.com.fiap.petbuddies.controller.atendimento.JanelaAtendimentoController;
import br.com.fiap.petbuddies.controller.cadastro.VeterinarioController;
import br.com.fiap.petbuddies.domain.entity.JanelaAtendimentoEntity;
import br.com.fiap.petbuddies.dto.atendimento.JanelaAtendimentoResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class JanelaAtendimentoModelAssembler
        implements RepresentationModelAssembler<JanelaAtendimentoEntity, EntityModel<JanelaAtendimentoResponse>> {

    @Override
    public EntityModel<JanelaAtendimentoResponse> toModel(JanelaAtendimentoEntity j) {
        JanelaAtendimentoResponse dto = JanelaAtendimentoResponse.from(j);
        EntityModel<JanelaAtendimentoResponse> model = EntityModel.of(
                dto,
                linkTo(methodOn(JanelaAtendimentoController.class).buscarPorId(j.getId())).withSelfRel(),
                linkTo(methodOn(JanelaAtendimentoController.class).listar(null)).withRel("janelas-atendimento"),
                linkTo(methodOn(VeterinarioController.class).buscarPorId(dto.getVeterinarioId())).withRel("veterinario"));
        if (dto.getConsultaId() != null) {
            model.add(linkTo(methodOn(ConsultaController.class).buscarPorId(dto.getConsultaId())).withRel("consulta"));
        }
        return model;
    }
}
