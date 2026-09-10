package br.com.fiap.petbuddies.controller;

import br.com.fiap.petbuddies.assembler.ClinicaModelAssembler;
import br.com.fiap.petbuddies.dto.cadastro.ClinicaRequest;
import br.com.fiap.petbuddies.dto.cadastro.ClinicaResponse;
import br.com.fiap.petbuddies.service.cadastro.ClinicaService;
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
@RequestMapping("/api/clinicas")
@Tag(name = "registro — clínicas", description = "CRUD de clínicas, a raiz do registro clínico")
public class ClinicaController {

    private final ClinicaService service;
    private final ClinicaModelAssembler assembler;

    public ClinicaController(ClinicaService service, ClinicaModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    @GetMapping
    @Operation(summary = "Lista clínicas")
    @ApiResponse(responseCode = "200", description = "Lista de clínicas")
    public CollectionModel<EntityModel<ClinicaResponse>> listar() {
        return assembler.toCollectionModel(service.listar());
    }

    @GetMapping("/buscar")
    @Operation(
        summary = "Busca clínica por CNPJ",
        description = "O CNPJ é único (UK_CLINICA_CNPJ), então a busca devolve no máximo uma clínica."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Clínica encontrada"),
        @ApiResponse(responseCode = "404", description = "Clínica não encontrada")
    })
    public EntityModel<ClinicaResponse> buscarPorCnpj(
            @Parameter(description = "CNPJ com 14 dígitos, sem pontuação")
            @RequestParam String cnpj) {
        return assembler.toModel(service.buscarPorCnpj(cnpj));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca clínica por id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Clínica encontrada"),
        @ApiResponse(responseCode = "404", description = "Clínica não encontrada")
    })
    public EntityModel<ClinicaResponse> buscarPorId(@PathVariable Long id) {
        return assembler.toModel(service.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Cria clínica", description = "Retorna 201 com a clínica criada. CNPJ já cadastrado é 409.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Clínica criada"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "409", description = "CNPJ já cadastrado")
    })
    public ResponseEntity<EntityModel<ClinicaResponse>> criar(@RequestBody @Valid ClinicaRequest request) {
        EntityModel<ClinicaResponse> model = assembler.toModel(service.criar(request));
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza clínica")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Clínica atualizada"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Clínica não encontrada"),
        @ApiResponse(responseCode = "409", description = "CNPJ já cadastrado em outra clínica")
    })
    public EntityModel<ClinicaResponse> atualizar(@PathVariable Long id, @RequestBody @Valid ClinicaRequest request) {
        return assembler.toModel(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove clínica")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Clínica removida"),
        @ApiResponse(responseCode = "404", description = "Clínica não encontrada")
    })
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
