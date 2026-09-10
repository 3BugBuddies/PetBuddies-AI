package br.com.fiap.petbuddies.controller;

import br.com.fiap.petbuddies.assembler.ProcedimentoModelAssembler;
import br.com.fiap.petbuddies.dto.atendimento.ProcedimentoRequest;
import br.com.fiap.petbuddies.dto.atendimento.ProcedimentoResponse;
import br.com.fiap.petbuddies.service.atendimento.ProcedimentoService;
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
@RequestMapping("/api/procedimentos")
@Tag(name = "registro — procedimentos", description = "Vacina, exame ou cirurgia executados num atendimento")
public class ProcedimentoController {

    private final ProcedimentoService service;
    private final ProcedimentoModelAssembler assembler;

    public ProcedimentoController(ProcedimentoService service, ProcedimentoModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    @GetMapping
    @Operation(
        summary = "Lista procedimentos",
        description = "Sem parâmetro, lista todos. Com animalId, da mais recente para a mais antiga; com registroAtendimentoId, filtra pelo atendimento."
    )
    @ApiResponse(responseCode = "200", description = "Lista de procedimentos")
    public CollectionModel<EntityModel<ProcedimentoResponse>> listar(
            @Parameter(description = "Id do animal")
            @RequestParam(required = false) Long animalId,
            @Parameter(description = "Id do registro de atendimento")
            @RequestParam(required = false) Long registroAtendimentoId) {
        return assembler.toCollectionModel(service.listar(animalId, registroAtendimentoId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca procedimento por id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Procedimento encontrado"),
        @ApiResponse(responseCode = "404", description = "Procedimento não encontrado")
    })
    public EntityModel<ProcedimentoResponse> buscarPorId(@PathVariable Long id) {
        return assembler.toModel(service.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Cria procedimento", description = "Sem status no corpo, o procedimento nasce PENDENTE.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Procedimento criado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Registro de atendimento, animal ou veterinário não encontrado")
    })
    public ResponseEntity<EntityModel<ProcedimentoResponse>> criar(@RequestBody @Valid ProcedimentoRequest request) {
        EntityModel<ProcedimentoResponse> model = assembler.toModel(service.criar(request));
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza procedimento", description = "É por aqui que o status muda, inclusive para REALIZADO e CANCELADO.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Procedimento atualizado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Procedimento, registro de atendimento, animal ou veterinário não encontrado")
    })
    public EntityModel<ProcedimentoResponse> atualizar(@PathVariable Long id, @RequestBody @Valid ProcedimentoRequest request) {
        return assembler.toModel(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove procedimento")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Procedimento removido"),
        @ApiResponse(responseCode = "404", description = "Procedimento não encontrado")
    })
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
