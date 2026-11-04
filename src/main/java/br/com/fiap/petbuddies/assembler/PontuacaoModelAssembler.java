package br.com.fiap.petbuddies.assembler;

import br.com.fiap.petbuddies.controller.atendimento.ConsultaController;
import br.com.fiap.petbuddies.controller.cadastro.AnimalController;
import br.com.fiap.petbuddies.controller.pontuacao.PontuacaoController;
import br.com.fiap.petbuddies.dto.pontuacao.LancamentoPontosResponse;
import br.com.fiap.petbuddies.dto.pontuacao.MemoriaCalculoResponse;
import br.com.fiap.petbuddies.dto.pontuacao.SaldoPontosResponse;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

// Os links apontam para rotas do Java: o app navega sem saber que os pontos moram no .NET.
@Component
public class PontuacaoModelAssembler {

    public EntityModel<SaldoPontosResponse> toModel(SaldoPontosResponse saldo) {
        return EntityModel.of(
                saldo,
                linkTo(methodOn(PontuacaoController.class).saldo(null)).withSelfRel(),
                linkTo(methodOn(PontuacaoController.class).extrato(null)).withRel("extrato"));
    }

    public EntityModel<LancamentoPontosResponse> toModel(LancamentoPontosResponse lancamento) {
        return EntityModel.of(
                lancamento,
                linkTo(methodOn(PontuacaoController.class).memoria(lancamento.consultaId(), null)).withRel("memoria"),
                linkTo(methodOn(ConsultaController.class).buscarPorId(lancamento.consultaId())).withRel("consulta"),
                linkTo(methodOn(AnimalController.class).buscarPorId(lancamento.animalId())).withRel("animal"));
    }

    public CollectionModel<EntityModel<LancamentoPontosResponse>> toCollectionModel(List<LancamentoPontosResponse> extrato) {
        return CollectionModel.of(
                extrato.stream().map(this::toModel).toList(),
                linkTo(methodOn(PontuacaoController.class).extrato(null)).withSelfRel(),
                linkTo(methodOn(PontuacaoController.class).saldo(null)).withRel("saldo"));
    }

    public EntityModel<MemoriaCalculoResponse> toModel(MemoriaCalculoResponse memoria) {
        return EntityModel.of(
                memoria,
                linkTo(methodOn(PontuacaoController.class).memoria(memoria.consultaId(), null)).withSelfRel(),
                linkTo(methodOn(ConsultaController.class).buscarPorId(memoria.consultaId())).withRel("consulta"));
    }
}
