package br.com.fiap.petbuddies.controller;

import br.com.fiap.petbuddies.assembler.ConsultaModelAssembler;
import br.com.fiap.petbuddies.dto.atendimento.AgendamentoRequest;
import br.com.fiap.petbuddies.dto.atendimento.CancelamentoRequest;
import br.com.fiap.petbuddies.assembler.FechamentoAtendimentoModelAssembler;
import br.com.fiap.petbuddies.dto.atendimento.ConsultaRequest;
import br.com.fiap.petbuddies.dto.atendimento.ConsultaResponse;
import br.com.fiap.petbuddies.dto.atendimento.FechamentoAtendimentoRequest;
import br.com.fiap.petbuddies.dto.atendimento.FechamentoAtendimentoResponse;
import br.com.fiap.petbuddies.service.ConsultaService;
import br.com.fiap.petbuddies.service.FechamentoAtendimentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/consultas")
@Tag(name = "registro — consultas", description = "Agendamento e comparecimento do animal na clínica")
public class ConsultaController {

    private final ConsultaService service;
    private final ConsultaModelAssembler assembler;
    private final FechamentoAtendimentoService fechamentoService;
    private final FechamentoAtendimentoModelAssembler fechamentoAssembler;

    public ConsultaController(
            ConsultaService service,
            ConsultaModelAssembler assembler,
            FechamentoAtendimentoService fechamentoService,
            FechamentoAtendimentoModelAssembler fechamentoAssembler) {
        this.service = service;
        this.assembler = assembler;
        this.fechamentoService = fechamentoService;
        this.fechamentoAssembler = fechamentoAssembler;
    }

    @GetMapping
    @Operation(
        summary = "Lista consultas",
        description = "Sem parâmetro, lista todas. Com animalId ou veterinarioId, filtra, da mais recente para a mais antiga."
    )
    @ApiResponse(responseCode = "200", description = "Lista de consultas")
    public CollectionModel<EntityModel<ConsultaResponse>> listar(
            @Parameter(description = "Id do animal")
            @RequestParam(required = false) Long animalId,
            @Parameter(description = "Id do veterinário")
            @RequestParam(required = false) Long veterinarioId) {
        return assembler.toCollectionModel(service.listar(animalId, veterinarioId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca consulta por id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Consulta encontrada"),
        @ApiResponse(responseCode = "404", description = "Consulta não encontrada")
    })
    public EntityModel<ConsultaResponse> buscarPorId(@PathVariable Long id) {
        return assembler.toModel(service.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Agenda consulta", description = "Sem status no corpo, a consulta nasce AGENDADA.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Consulta agendada"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Animal ou veterinário não encontrado")
    })
    public ResponseEntity<EntityModel<ConsultaResponse>> criar(@RequestBody @Valid ConsultaRequest request) {
        EntityModel<ConsultaResponse> model = assembler.toModel(service.criar(request));
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Atualiza consulta",
        description = "É por aqui que o status muda, inclusive para REALIZADA, CANCELADA e NAO_COMPARECEU."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Consulta atualizada"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Consulta, animal ou veterinário não encontrado")
    })
    public EntityModel<ConsultaResponse> atualizar(@PathVariable Long id, @RequestBody @Valid ConsultaRequest request) {
        return assembler.toModel(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove consulta")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Consulta removida"),
        @ApiResponse(responseCode = "404", description = "Consulta não encontrada")
    })
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/agendamentos")
    @Operation(
        summary = "Agenda consulta numa janela livre",
        description = "Ocupa a janela de atendimento informada; a consulta nasce AGENDADA, com data/hora e "
            + "veterinário herdados do slot, não do corpo da requisição."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Consulta agendada"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou janela no passado"),
        @ApiResponse(responseCode = "404", description = "Janela ou animal não encontrado"),
        @ApiResponse(responseCode = "409", description = "Janela já ocupada por outra consulta")
    })
    public ResponseEntity<EntityModel<ConsultaResponse>> agendar(@RequestBody @Valid AgendamentoRequest request) {
        EntityModel<ConsultaResponse> model = assembler.toModel(service.agendar(request));
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @PostMapping("/{id}/cancelamento")
    @Operation(
        summary = "Cancela consulta",
        description = "Marca a consulta como CANCELADA com o motivo informado e devolve a janela de "
            + "atendimento vinculada ao pool de horários livres."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Consulta cancelada"),
        @ApiResponse(responseCode = "400", description = "Motivo do cancelamento ausente"),
        @ApiResponse(responseCode = "404", description = "Consulta não encontrada"),
        @ApiResponse(responseCode = "409", description = "Consulta já realizada")
    })
    public EntityModel<ConsultaResponse> cancelar(@PathVariable Long id, @RequestBody @Valid CancelamentoRequest request) {
        return assembler.toModel(service.cancelar(id, request));
    }
    @PostMapping("/{id}/fechamento")
    @Operation(
        summary = "Fecha o atendimento",
        description = "Ato único e transacional: grava o registro do atendimento, os procedimentos "
            + "executados e as prescrições assinadas com suas regras condicionais, e muda a consulta para "
            + "REALIZADA. Ou tudo grava, ou nada grava. O animal e o veterinário vêm da própria consulta."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Atendimento fechado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos, inclusive faixa de dose ou regra incoerente"),
        @ApiResponse(responseCode = "404", description = "Consulta ou condição clínica não encontrada"),
        @ApiResponse(responseCode = "409", description = "Consulta já REALIZADA, CANCELADA ou NAO_COMPARECEU")
    })
    public ResponseEntity<EntityModel<FechamentoAtendimentoResponse>> fechar(
            @PathVariable Long id, @RequestBody @Valid FechamentoAtendimentoRequest request) {
        EntityModel<FechamentoAtendimentoResponse> model =
                fechamentoAssembler.toModel(fechamentoService.fechar(id, request));
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }
}
