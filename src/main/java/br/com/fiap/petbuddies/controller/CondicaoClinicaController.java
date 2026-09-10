package br.com.fiap.petbuddies.controller;

import br.com.fiap.petbuddies.assembler.CondicaoClinicaModelAssembler;
import br.com.fiap.petbuddies.dto.atendimento.CondicaoClinicaRequest;
import br.com.fiap.petbuddies.dto.atendimento.CondicaoClinicaResponse;
import br.com.fiap.petbuddies.service.CondicaoClinicaService;
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
@RequestMapping("/api/condicoes-clinicas")
@Tag(name = "registro — condições clínicas", description = "Catálogo de condições que o check-in avalia, por clínica")
public class CondicaoClinicaController {

    private final CondicaoClinicaService service;
    private final CondicaoClinicaModelAssembler assembler;

    public CondicaoClinicaController(CondicaoClinicaService service, CondicaoClinicaModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    @GetMapping
    @Operation(
        summary = "Lista condições clínicas",
        description = "Sem parâmetro, lista todas. Com clinicaId, lista só as da clínica."
    )
    @ApiResponse(responseCode = "200", description = "Lista de condições clínicas")
    public CollectionModel<EntityModel<CondicaoClinicaResponse>> listar(
            @Parameter(description = "Id da clínica")
            @RequestParam(required = false) Long clinicaId) {
        return assembler.toCollectionModel(service.listar(clinicaId));
    }

    @GetMapping("/buscar")
    @Operation(
        summary = "Busca condição clínica por código",
        description = "O código é único dentro da clínica (UK_CONDICAO_CLINICA_CODIGO), então os dois parâmetros são obrigatórios."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Condição clínica encontrada"),
        @ApiResponse(responseCode = "400", description = "Parâmetro obrigatório ausente"),
        @ApiResponse(responseCode = "404", description = "Condição clínica não encontrada")
    })
    public EntityModel<CondicaoClinicaResponse> buscarPorCodigo(
            @Parameter(description = "Id da clínica") @RequestParam Long clinicaId,
            @Parameter(description = "Código da condição") @RequestParam String codigo) {
        return assembler.toModel(service.buscarPorCodigo(clinicaId, codigo));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca condição clínica por id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Condição clínica encontrada"),
        @ApiResponse(responseCode = "404", description = "Condição clínica não encontrada")
    })
    public EntityModel<CondicaoClinicaResponse> buscarPorId(@PathVariable Long id) {
        return assembler.toModel(service.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Cria condição clínica", description = "Código já usado na mesma clínica é 409.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Condição clínica criada"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Clínica ou veterinário não encontrado"),
        @ApiResponse(responseCode = "409", description = "Código já usado nesta clínica")
    })
    public ResponseEntity<EntityModel<CondicaoClinicaResponse>> criar(@RequestBody @Valid CondicaoClinicaRequest request) {
        EntityModel<CondicaoClinicaResponse> model = assembler.toModel(service.criar(request));
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza condição clínica")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Condição clínica atualizada"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Condição clínica, clínica ou veterinário não encontrado"),
        @ApiResponse(responseCode = "409", description = "Código já usado em outra condição da mesma clínica")
    })
    public EntityModel<CondicaoClinicaResponse> atualizar(
            @PathVariable Long id, @RequestBody @Valid CondicaoClinicaRequest request) {
        return assembler.toModel(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove condição clínica")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Condição clínica removida"),
        @ApiResponse(responseCode = "404", description = "Condição clínica não encontrada")
    })
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
