package br.com.fiap.petbuddies.controller;

import br.com.fiap.petbuddies.assembler.ConsultaModelAssembler;
import br.com.fiap.petbuddies.dto.ConsultaRequest;
import br.com.fiap.petbuddies.dto.ConsultaResponse;
import br.com.fiap.petbuddies.service.ConsultaService;
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

    public ConsultaController(ConsultaService service, ConsultaModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
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
}
