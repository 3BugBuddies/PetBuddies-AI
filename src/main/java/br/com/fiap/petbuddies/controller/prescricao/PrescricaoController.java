package br.com.fiap.petbuddies.controller.prescricao;

import br.com.fiap.petbuddies.assembler.PrescricaoModelAssembler;
import br.com.fiap.petbuddies.dto.prescricao.PrescricaoRequest;
import br.com.fiap.petbuddies.dto.prescricao.PrescricaoResponse;
import br.com.fiap.petbuddies.service.prescricao.PrescricaoService;
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

/** Sem PUT nem DELETE: a prescrição é imutável depois de assinada (ADR s3-09). */
@RestController
@RequestMapping("/api/prescricoes")
@Tag(name = "registro — prescrições", description = "O ato assinado pelo veterinário — imutável depois de criado")
public class PrescricaoController {

    private final PrescricaoService service;
    private final PrescricaoModelAssembler assembler;

    public PrescricaoController(PrescricaoService service, PrescricaoModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    @GetMapping
    @Operation(
        summary = "Lista prescrições",
        description = "Sem parâmetro, lista todas. Com animalId, da mais recente para a mais antiga; com registroAtendimentoId, filtra pelo atendimento."
    )
    @ApiResponse(responseCode = "200", description = "Lista de prescrições")
    public CollectionModel<EntityModel<PrescricaoResponse>> listar(
            @Parameter(description = "Id do animal")
            @RequestParam(required = false) Long animalId,
            @Parameter(description = "Id do registro de atendimento")
            @RequestParam(required = false) Long registroAtendimentoId) {
        return assembler.toCollectionModel(service.listar(animalId, registroAtendimentoId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca prescrição por id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Prescrição encontrada"),
        @ApiResponse(responseCode = "404", description = "Prescrição não encontrada")
    })
    public EntityModel<PrescricaoResponse> buscarPorId(@PathVariable Long id) {
        return assembler.toModel(service.buscarPorId(id));
    }

    @PostMapping
    @Operation(
        summary = "Assina prescrição",
        description = "Cria o ato assinado pelo veterinário. Não há PUT nem DELETE: corrigir significa assinar uma nova prescrição."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Prescrição assinada"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos, inclusive faixa de dose invertida"),
        @ApiResponse(responseCode = "404", description = "Animal, veterinário ou registro de atendimento não encontrado")
    })
    public ResponseEntity<EntityModel<PrescricaoResponse>> criar(@RequestBody @Valid PrescricaoRequest request) {
        EntityModel<PrescricaoResponse> model = assembler.toModel(service.criar(request));
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }
}
