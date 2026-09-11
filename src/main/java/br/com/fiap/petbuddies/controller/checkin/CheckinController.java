package br.com.fiap.petbuddies.controller.checkin;

import br.com.fiap.petbuddies.assembler.CheckinModelAssembler;
import br.com.fiap.petbuddies.dto.checkin.CheckinExtracaoRequest;
import br.com.fiap.petbuddies.dto.checkin.CheckinExtracaoResponse;
import br.com.fiap.petbuddies.dto.checkin.CheckinRequest;
import br.com.fiap.petbuddies.dto.checkin.CheckinResponse;
import br.com.fiap.petbuddies.service.checkin.CheckinExtracaoService;
import br.com.fiap.petbuddies.service.checkin.CheckinService;
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

// Dois passos: /extracao interpreta e nao grava nada; o POST raiz grava o que o tutor confirmou e avalia a regra. Nunca o contrario.
@RestController
@RequestMapping("/api/checkin")
@Tag(name = "cuidado — check-in", description = "Check-in narrado do tutor, com extração por IA")
public class CheckinController {

    private final CheckinExtracaoService extracaoService;
    private final CheckinService checkinService;
    private final CheckinModelAssembler assembler;

    public CheckinController(CheckinExtracaoService extracaoService, CheckinService checkinService, CheckinModelAssembler assembler) {
        this.extracaoService = extracaoService;
        this.checkinService = checkinService;
        this.assembler = assembler;
    }

    @PostMapping("/extracao")
    @Operation(
        summary = "Interpreta a narrativa (passo 1)",
        description = "Roda o modelo contra o vocabulário de condições em vigor para o animal e devolve o que "
            + "entendeu. Não grava nada — a resposta não vem em envelope HATEOAS pelo mesmo motivo do login: "
            + "não é recurso navegável. degradado=true quando o modelo falhou ou não devolveu nada aproveitável; "
            + "o app deve cair para as perguntas fixas nesse caso."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Extração realizada"),
        @ApiResponse(responseCode = "404", description = "Animal não encontrado")
    })
    public CheckinExtracaoResponse extrair(@RequestBody @Valid CheckinExtracaoRequest request) {
        return extracaoService.extrair(request);
    }

    @PostMapping
    @Operation(
        summary = "Registra o check-in confirmado (passo 2)",
        description = "Grava a narrativa e as condições que o tutor confirmou, avalia a regra congelada de cada "
            + "prescrição em vigor e grava o desfecho no item do plano quando ele existe. Determinístico: nenhuma "
            + "chamada ao modelo acontece aqui."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Check-in registrado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos, inclusive valor incoerente com o tipo da condição"),
        @ApiResponse(responseCode = "404", description = "Animal, item de plano ou condição clínica não encontrado"),
        @ApiResponse(responseCode = "409", description = "Já existe check-in para este animal, data e item")
    })
    public ResponseEntity<EntityModel<CheckinResponse>> registrar(@RequestBody @Valid CheckinRequest request) {
        EntityModel<CheckinResponse> model = assembler.toModel(checkinService.registrar(request));
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca check-in por id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Check-in encontrado"),
        @ApiResponse(responseCode = "404", description = "Check-in não encontrado")
    })
    public EntityModel<CheckinResponse> buscarPorId(@PathVariable Long id) {
        return assembler.toModel(checkinService.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Lista check-ins de um animal", description = "Da mais recente para a mais antiga.")
    @ApiResponse(responseCode = "200", description = "Lista de check-ins")
    public CollectionModel<EntityModel<CheckinResponse>> listar(
            @Parameter(description = "Id do animal") @RequestParam Long animalId) {
        return assembler.toCollectionModel(checkinService.listar(animalId));
    }
}
