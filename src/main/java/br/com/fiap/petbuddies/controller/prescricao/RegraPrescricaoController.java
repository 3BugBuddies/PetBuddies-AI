package br.com.fiap.petbuddies.controller.prescricao;

import br.com.fiap.petbuddies.assembler.RegraPrescricaoModelAssembler;
import br.com.fiap.petbuddies.dto.prescricao.RegraPrescricaoRequest;
import br.com.fiap.petbuddies.dto.prescricao.RegraPrescricaoResponse;
import br.com.fiap.petbuddies.service.prescricao.RegraPrescricaoService;
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

/** Sem PUT nem DELETE: a regra é imutável como a prescrição que a carrega. */
@RestController
@RequestMapping("/api/regra-prescricao")
@Tag(name = "registro — regras de prescrição", description = "Condição → ação sobre a dose, com a condição congelada no momento da assinatura")
public class RegraPrescricaoController {

    private final RegraPrescricaoService service;
    private final RegraPrescricaoModelAssembler assembler;

    public RegraPrescricaoController(RegraPrescricaoService service, RegraPrescricaoModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    @GetMapping
    @Operation(
        summary = "Lista regras de prescrição",
        description = "Sem parâmetro, lista todas. Com prescricaoId, filtra e ordena pela ordem de avaliação assinada."
    )
    @ApiResponse(responseCode = "200", description = "Lista de regras de prescrição")
    public CollectionModel<EntityModel<RegraPrescricaoResponse>> listar(
            @Parameter(description = "Id da prescrição")
            @RequestParam(required = false) Long prescricaoId) {
        return assembler.toCollectionModel(service.listar(prescricaoId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca regra de prescrição por id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Regra encontrada"),
        @ApiResponse(responseCode = "404", description = "Regra não encontrada")
    })
    public EntityModel<RegraPrescricaoResponse> buscarPorId(@PathVariable Long id) {
        return assembler.toModel(service.buscarPorId(id));
    }

    @PostMapping
    @Operation(
        summary = "Cria regra de prescrição",
        description = "Congela rótulo, tipo de dado e fonte da condição clínica no momento da criação. Não há PUT nem DELETE."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Regra criada"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos, inclusive operador/limite incoerentes com o tipo da condição"),
        @ApiResponse(responseCode = "404", description = "Prescrição ou condição clínica não encontrada")
    })
    public ResponseEntity<EntityModel<RegraPrescricaoResponse>> criar(@RequestBody @Valid RegraPrescricaoRequest request) {
        EntityModel<RegraPrescricaoResponse> model = assembler.toModel(service.criar(request));
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }
}
