package br.com.fiap.petbuddies.controller;

import br.com.fiap.petbuddies.assembler.ResponsavelModelAssembler;
import br.com.fiap.petbuddies.dto.cadastro.ResponsavelRequest;
import br.com.fiap.petbuddies.dto.cadastro.ResponsavelResponse;
import br.com.fiap.petbuddies.service.cadastro.ResponsavelService;
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
@RequestMapping("/api/responsaveis")
@Tag(name = "registro — responsáveis", description = "CRUD de tutores, o dono do animal no registro clínico")
public class ResponsavelController {

    private final ResponsavelService service;
    private final ResponsavelModelAssembler assembler;

    public ResponsavelController(ResponsavelService service, ResponsavelModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    @GetMapping
    @Operation(
        summary = "Lista responsáveis",
        description = "Sem parâmetro, lista todos. Com nome, filtra por trecho do nome, sem diferenciar maiúsculas."
    )
    @ApiResponse(responseCode = "200", description = "Lista de responsáveis")
    public CollectionModel<EntityModel<ResponsavelResponse>> listar(
            @Parameter(description = "Trecho do nome do responsável")
            @RequestParam(required = false) String nome) {
        return assembler.toCollectionModel(service.listar(nome));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca responsável por id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Responsável encontrado"),
        @ApiResponse(responseCode = "404", description = "Responsável não encontrado")
    })
    public EntityModel<ResponsavelResponse> buscarPorId(@PathVariable Long id) {
        return assembler.toModel(service.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Cria responsável", description = "Retorna 201 com o responsável criado.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Responsável criado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<EntityModel<ResponsavelResponse>> criar(@RequestBody @Valid ResponsavelRequest request) {
        EntityModel<ResponsavelResponse> model = assembler.toModel(service.criar(request));
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza responsável")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Responsável atualizado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Responsável não encontrado")
    })
    public EntityModel<ResponsavelResponse> atualizar(@PathVariable Long id, @RequestBody @Valid ResponsavelRequest request) {
        return assembler.toModel(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove responsável")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Responsável removido"),
        @ApiResponse(responseCode = "404", description = "Responsável não encontrado")
    })
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
