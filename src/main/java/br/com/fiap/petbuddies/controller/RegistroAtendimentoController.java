package br.com.fiap.petbuddies.controller;

import br.com.fiap.petbuddies.assembler.RegistroAtendimentoModelAssembler;
import br.com.fiap.petbuddies.dto.RegistroAtendimentoRequest;
import br.com.fiap.petbuddies.dto.RegistroAtendimentoResponse;
import br.com.fiap.petbuddies.service.RegistroAtendimentoService;
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
@RequestMapping("/api/registros-atendimento")
@Tag(name = "registro — registros de atendimento", description = "O que aconteceu na consulta: anamnese, diagnóstico e tratamento")
public class RegistroAtendimentoController {

    private final RegistroAtendimentoService service;
    private final RegistroAtendimentoModelAssembler assembler;

    public RegistroAtendimentoController(RegistroAtendimentoService service, RegistroAtendimentoModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    @GetMapping
    @Operation(
        summary = "Lista registros de atendimento",
        description = "Sem parâmetro, lista todos. Com animalId, da mais recente para a mais antiga; com consultaId, filtra pela consulta."
    )
    @ApiResponse(responseCode = "200", description = "Lista de registros de atendimento")
    public CollectionModel<EntityModel<RegistroAtendimentoResponse>> listar(
            @Parameter(description = "Id do animal")
            @RequestParam(required = false) Long animalId,
            @Parameter(description = "Id da consulta")
            @RequestParam(required = false) Long consultaId) {
        return assembler.toCollectionModel(service.listar(animalId, consultaId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca registro de atendimento por id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Registro encontrado"),
        @ApiResponse(responseCode = "404", description = "Registro não encontrado")
    })
    public EntityModel<RegistroAtendimentoResponse> buscarPorId(@PathVariable Long id) {
        return assembler.toModel(service.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Cria registro de atendimento", description = "É o pai de procedimento e prescrição: sem ele, os dois níveis seguintes não existem.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Registro criado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Animal ou consulta não encontrado")
    })
    public ResponseEntity<EntityModel<RegistroAtendimentoResponse>> criar(@RequestBody @Valid RegistroAtendimentoRequest request) {
        EntityModel<RegistroAtendimentoResponse> model = assembler.toModel(service.criar(request));
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza registro de atendimento")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Registro atualizado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Registro, animal ou consulta não encontrado")
    })
    public EntityModel<RegistroAtendimentoResponse> atualizar(@PathVariable Long id, @RequestBody @Valid RegistroAtendimentoRequest request) {
        return assembler.toModel(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove registro de atendimento")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Registro removido"),
        @ApiResponse(responseCode = "404", description = "Registro não encontrado")
    })
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
