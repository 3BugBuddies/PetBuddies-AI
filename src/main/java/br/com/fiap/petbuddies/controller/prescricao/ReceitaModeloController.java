package br.com.fiap.petbuddies.controller.prescricao;

import br.com.fiap.petbuddies.assembler.ReceitaModeloModelAssembler;
import br.com.fiap.petbuddies.dto.prescricao.ReceitaModeloRequest;
import br.com.fiap.petbuddies.dto.prescricao.ReceitaModeloResponse;
import br.com.fiap.petbuddies.service.prescricao.ReceitaModeloService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/receita-modelo")
@Tag(name = "registro — receitas-modelo", description = "Molde de receita da clínica. A prescrição copia o conteúdo; não há referência viva")
public class ReceitaModeloController {

    private final ReceitaModeloService service;
    private final ReceitaModeloModelAssembler assembler;

    public ReceitaModeloController(ReceitaModeloService service, ReceitaModeloModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    @GetMapping
    @Operation(
        summary = "Lista as receitas-modelo ativas da clínica",
        description = "A clínica vem do usuário autenticado. Ordenadas por título; as desativadas não aparecem."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de receitas-modelo"),
        @ApiResponse(responseCode = "403", description = "Usuário sem vínculo com veterinário")
    })
    public CollectionModel<EntityModel<ReceitaModeloResponse>> listar(Authentication autenticacao) {
        return assembler.toCollectionModel(service.listar(usuarioId(autenticacao)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca receita-modelo por id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Receita-modelo encontrada"),
        @ApiResponse(responseCode = "404", description = "Não encontrada, de outra clínica ou desativada")
    })
    public EntityModel<ReceitaModeloResponse> buscarPorId(@PathVariable Long id, Authentication autenticacao) {
        return assembler.toModel(service.buscarPorId(id, usuarioId(autenticacao)));
    }

    @PostMapping
    @Operation(
        summary = "Cria receita-modelo",
        description = "Clínica e autor vêm do usuário autenticado. A ordem das regras é a da lista."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Receita-modelo criada"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Condição clínica não encontrada"),
        @ApiResponse(responseCode = "409", description = "Título já usado na clínica"),
        @ApiResponse(responseCode = "422", description = "Regra incoerente com a condição")
    })
    public ResponseEntity<EntityModel<ReceitaModeloResponse>> criar(
            @RequestBody @Valid ReceitaModeloRequest request, Authentication autenticacao) {
        EntityModel<ReceitaModeloResponse> model = assembler.toModel(service.criar(request, usuarioId(autenticacao)));
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Atualiza receita-modelo",
        description = "Substitui conteúdo e regras e incrementa a versão. Receita já assinada não muda."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Receita-modelo atualizada"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Receita-modelo ou condição não encontrada"),
        @ApiResponse(responseCode = "409", description = "Título já usado na clínica"),
        @ApiResponse(responseCode = "422", description = "Regra incoerente com a condição")
    })
    public EntityModel<ReceitaModeloResponse> atualizar(
            @PathVariable Long id, @RequestBody @Valid ReceitaModeloRequest request, Authentication autenticacao) {
        return assembler.toModel(service.atualizar(id, request, usuarioId(autenticacao)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativa receita-modelo", description = "Não apaga: marca como inativa.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Desativada"),
        @ApiResponse(responseCode = "404", description = "Receita-modelo não encontrada")
    })
    public ResponseEntity<Void> desativar(@PathVariable Long id, Authentication autenticacao) {
        service.desativar(id, usuarioId(autenticacao));
        return ResponseEntity.noContent().build();
    }

    private static Long usuarioId(Authentication autenticacao) {
        return Long.valueOf(autenticacao.getName());
    }
}
