package br.com.fiap.petbuddies.controller;

import br.com.fiap.petbuddies.assembler.JanelaAtendimentoModelAssembler;
import br.com.fiap.petbuddies.dto.atendimento.JanelaAtendimentoRequest;
import br.com.fiap.petbuddies.dto.atendimento.JanelaAtendimentoResponse;
import br.com.fiap.petbuddies.service.atendimento.JanelaAtendimentoService;
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

import java.time.LocalDate;

@RestController
@RequestMapping("/api/janelas-atendimento")
@Tag(name = "registro — janelas de atendimento", description = "Agenda do veterinário: slots de 30 minutos, livres ou reservados por uma consulta")
public class JanelaAtendimentoController {

    private final JanelaAtendimentoService service;
    private final JanelaAtendimentoModelAssembler assembler;

    public JanelaAtendimentoController(JanelaAtendimentoService service, JanelaAtendimentoModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    @GetMapping
    @Operation(
        summary = "Lista janelas de atendimento",
        description = "Sem parâmetro, lista todas. Com veterinarioId, filtra e ordena da mais cedo para a mais tarde."
    )
    @ApiResponse(responseCode = "200", description = "Lista de janelas de atendimento")
    public CollectionModel<EntityModel<JanelaAtendimentoResponse>> listar(
            @Parameter(description = "Id do veterinário")
            @RequestParam(required = false) Long veterinarioId) {
        return assembler.toCollectionModel(service.listar(veterinarioId));
    }

    @GetMapping("/livres")
    @Operation(
        summary = "Lista janelas livres de um veterinário num dia",
        description = "Slots sem consulta vinculada do veterinário informado, dentro do dia informado, "
            + "da mais cedo para a mais tarde — a leitura que a tela de agendamento faz primeiro."
    )
    @ApiResponse(responseCode = "200", description = "Lista de janelas livres")
    public CollectionModel<EntityModel<JanelaAtendimentoResponse>> listarLivres(
            @Parameter(description = "Id do veterinário", required = true)
            @RequestParam Long veterinarioId,
            @Parameter(description = "Dia, no formato AAAA-MM-DD", required = true)
            @RequestParam LocalDate data) {
        return assembler.toCollectionModel(service.listarLivres(veterinarioId, data));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca janela de atendimento por id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Janela encontrada"),
        @ApiResponse(responseCode = "404", description = "Janela não encontrada")
    })
    public EntityModel<JanelaAtendimentoResponse> buscarPorId(@PathVariable Long id) {
        return assembler.toModel(service.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Abre janela de atendimento", description = "Sem consultaId no corpo, o slot nasce livre.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Janela aberta"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Veterinário ou consulta não encontrado"),
        @ApiResponse(responseCode = "409", description = "Veterinário já tem janela nesse horário")
    })
    public ResponseEntity<EntityModel<JanelaAtendimentoResponse>> criar(@RequestBody @Valid JanelaAtendimentoRequest request) {
        EntityModel<JanelaAtendimentoResponse> model = assembler.toModel(service.criar(request));
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza janela de atendimento", description = "É por aqui que a janela reserva ou libera o vínculo com a consulta.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Janela atualizada"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Janela, veterinário ou consulta não encontrado"),
        @ApiResponse(responseCode = "409", description = "Veterinário já tem janela nesse horário")
    })
    public EntityModel<JanelaAtendimentoResponse> atualizar(@PathVariable Long id, @RequestBody @Valid JanelaAtendimentoRequest request) {
        return assembler.toModel(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove janela de atendimento")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Janela removida"),
        @ApiResponse(responseCode = "404", description = "Janela não encontrada")
    })
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
