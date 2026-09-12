package br.com.fiap.petbuddies.controller.prescricao;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import br.com.fiap.petbuddies.assembler.PrescricaoModelAssembler;
import br.com.fiap.petbuddies.domain.entity.PrescricaoEntity;
import br.com.fiap.petbuddies.dto.prescricao.PrescricaoLoteRequest;
import br.com.fiap.petbuddies.dto.prescricao.PrescricaoResponse;
import br.com.fiap.petbuddies.service.prescricao.PrescricaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Sem PUT nem DELETE: a prescricao e imutavel depois de assinada.
@RestController
@RequestMapping("/api/prescricao")
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
        summary = "Assina prescrições",
        description = "Cria os atos assinados pelo veterinário numa transação só — uma prescrição é uma lista de um. "
                + "Todas precisam ser do mesmo registro de atendimento. Não há PUT nem DELETE: corrigir significa assinar de novo."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Prescrições assinadas"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos: lista vazia, faixa de dose invertida, ou atendimentos diferentes no mesmo lote"),
        @ApiResponse(responseCode = "404", description = "Animal, veterinário ou registro de atendimento não encontrado")
    })
    public ResponseEntity<CollectionModel<EntityModel<PrescricaoResponse>>> criar(
            @RequestBody @Valid PrescricaoLoteRequest request) {
        List<PrescricaoEntity> criadas = service.criarEmLote(request.getPrescricoes());

        // O self da colecao e o atendimento, nao um recurso: com N prescricoes nao
        // existe um Location unico. O @AssertTrue do envelope garante que o id e o mesmo.
        Long registroAtendimentoId = request.getPrescricoes().get(0).getRegistroAtendimentoId();
        Link self = linkTo(methodOn(PrescricaoController.class).listar(null, registroAtendimentoId)).withSelfRel();

        CollectionModel<EntityModel<PrescricaoResponse>> model = CollectionModel.of(
                criadas.stream().map(assembler::toModel).toList(), self);
        return ResponseEntity.created(self.toUri()).body(model);
    }
}
