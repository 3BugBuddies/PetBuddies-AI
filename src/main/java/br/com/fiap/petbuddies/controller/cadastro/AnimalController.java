package br.com.fiap.petbuddies.controller.cadastro;

import br.com.fiap.petbuddies.assembler.AnimalModelAssembler;
import br.com.fiap.petbuddies.dto.cadastro.AnimalRequest;
import br.com.fiap.petbuddies.dto.cadastro.AnimalResponse;
import br.com.fiap.petbuddies.service.cadastro.AnimalService;
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
@RequestMapping("/api/animal")
@Tag(name = "registro — animais", description = "CRUD de pacientes, o animal do registro clínico")
public class AnimalController {

    private final AnimalService service;
    private final AnimalModelAssembler assembler;

    public AnimalController(AnimalService service, AnimalModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    @GetMapping
    @Operation(
        summary = "Lista animais",
        description = "Sem parâmetro, lista todos. Com responsavelId, lista os do tutor; com nome, filtra por trecho do nome."
    )
    @ApiResponse(responseCode = "200", description = "Lista de animais")
    public CollectionModel<EntityModel<AnimalResponse>> listar(
            @Parameter(description = "Id do responsável")
            @RequestParam(required = false) Long responsavelId,
            @Parameter(description = "Trecho do nome do animal")
            @RequestParam(required = false) String nome) {
        return assembler.toCollectionModel(service.listar(responsavelId, nome));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca animal por id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Animal encontrado"),
        @ApiResponse(responseCode = "404", description = "Animal não encontrado")
    })
    public EntityModel<AnimalResponse> buscarPorId(@PathVariable Long id) {
        return assembler.toModel(service.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Cria animal", description = "Retorna 201 com o animal criado.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Animal criado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Responsável não encontrado")
    })
    public ResponseEntity<EntityModel<AnimalResponse>> criar(@RequestBody @Valid AnimalRequest request) {
        EntityModel<AnimalResponse> model = assembler.toModel(service.criar(request));
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza animal")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Animal atualizado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Animal ou responsável não encontrado")
    })
    public EntityModel<AnimalResponse> atualizar(@PathVariable Long id, @RequestBody @Valid AnimalRequest request) {
        return assembler.toModel(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove animal")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Animal removido"),
        @ApiResponse(responseCode = "404", description = "Animal não encontrado")
    })
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
