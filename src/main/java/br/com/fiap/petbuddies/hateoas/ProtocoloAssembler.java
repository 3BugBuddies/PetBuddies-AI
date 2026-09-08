package br.com.fiap.petbuddies.hateoas;

import br.com.fiap.petbuddies.controller.protocolo.EventoProtocoloController;
import br.com.fiap.petbuddies.controller.protocolo.ProtocoloController;
import br.com.fiap.petbuddies.dto.protocolo.ProtocoloResponse;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Envelopa a resposta de protocolo com os links que o consumidor precisa para
 * navegar sem montar URL a mao.
 *
 * <p>O ganho concreto e para o app: em vez de concatenar
 * {@code "/api/protocolos/" + id + "/eventos"} no cliente, ele segue
 * {@code _links.eventos.href}. Quando a rota mudar, o app nao muda.</p>
 */
@Component
public class ProtocoloAssembler {

    public EntityModel<ProtocoloResponse> toModel(ProtocoloResponse protocolo) {
        return EntityModel.of(protocolo,
                linkTo(methodOn(ProtocoloController.class).buscarPorId(protocolo.getId())).withSelfRel(),
                linkTo(methodOn(EventoProtocoloController.class)
                        .listar(protocolo.getId(), null)).withRel("eventos"),
                linkTo(methodOn(ProtocoloController.class).listar()).withRel("protocolos"));
    }

    public CollectionModel<EntityModel<ProtocoloResponse>> toCollectionModel(List<ProtocoloResponse> protocolos) {
        List<EntityModel<ProtocoloResponse>> itens = protocolos.stream().map(this::toModel).toList();
        return CollectionModel.of(itens,
                linkTo(methodOn(ProtocoloController.class).listar()).withSelfRel());
    }
}
