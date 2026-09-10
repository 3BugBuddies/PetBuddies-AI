package br.com.fiap.petbuddies.controller;

import br.com.fiap.petbuddies.assembler.VeterinarioModelAssembler;
import br.com.fiap.petbuddies.dto.cadastro.VeterinarioRequest;
import br.com.fiap.petbuddies.dto.cadastro.VeterinarioResponse;
import br.com.fiap.petbuddies.service.cadastro.VeterinarioService;
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
@RequestMapping("/api/veterinarios")
@Tag(name = "registro — veterinários", description = "CRUD da equipe clínica, quem assina o ato")
public class VeterinarioController {

    private final VeterinarioService service;
    private final VeterinarioModelAssembler assembler;

    public VeterinarioController(VeterinarioService service, VeterinarioModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    @GetMapping
    @Operation(
        summary = "Lista veterinários",
        description = "Sem parâmetro, lista todos. Com clinicaId, lista só os da clínica."
    )
    @ApiResponse(responseCode = "200", description = "Lista de veterinários")
    public CollectionModel<EntityModel<VeterinarioResponse>> listar(
            @Parameter(description = "Id da clínica")
            @RequestParam(required = false) Long clinicaId) {
        return assembler.toCollectionModel(service.listar(clinicaId));
    }

    @GetMapping("/buscar")
    @Operation(
        summary = "Busca veterinário por CRMV",
        description = "O CRMV é único (UK_VETERINARIO_CRMV), então a busca devolve no máximo um veterinário."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Veterinário encontrado"),
        @ApiResponse(responseCode = "404", description = "Veterinário não encontrado")
    })
    public EntityModel<VeterinarioResponse> buscarPorCrmv(
            @Parameter(description = "CRMV do veterinário")
            @RequestParam String crmv) {
        return assembler.toModel(service.buscarPorCrmv(crmv));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca veterinário por id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Veterinário encontrado"),
        @ApiResponse(responseCode = "404", description = "Veterinário não encontrado")
    })
    public EntityModel<VeterinarioResponse> buscarPorId(@PathVariable Long id) {
        return assembler.toModel(service.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Cria veterinário", description = "Retorna 201 com o veterinário criado. CRMV já cadastrado é 409.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Veterinário criado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Clínica não encontrada"),
        @ApiResponse(responseCode = "409", description = "CRMV já cadastrado")
    })
    public ResponseEntity<EntityModel<VeterinarioResponse>> criar(@RequestBody @Valid VeterinarioRequest request) {
        EntityModel<VeterinarioResponse> model = assembler.toModel(service.criar(request));
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza veterinário")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Veterinário atualizado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Veterinário ou clínica não encontrado"),
        @ApiResponse(responseCode = "409", description = "CRMV já cadastrado em outro veterinário")
    })
    public EntityModel<VeterinarioResponse> atualizar(@PathVariable Long id, @RequestBody @Valid VeterinarioRequest request) {
        return assembler.toModel(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove veterinário")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Veterinário removido"),
        @ApiResponse(responseCode = "404", description = "Veterinário não encontrado")
    })
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
