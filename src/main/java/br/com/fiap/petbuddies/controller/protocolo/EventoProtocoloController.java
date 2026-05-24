package br.com.fiap.petbuddies.controller.protocolo;

import br.com.fiap.petbuddies.domain.enums.TipoEventoProtocolo;
import br.com.fiap.petbuddies.dto.protocolo.EventoProtocoloRequest;
import br.com.fiap.petbuddies.dto.protocolo.EventoProtocoloResponse;
import br.com.fiap.petbuddies.service.protocolo.EventoProtocoloService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "catalogo — eventos de protocolo", description = "CRUD de eventos vinculados a protocolos de cuidado")
public class EventoProtocoloController {

    private final EventoProtocoloService service;

    public EventoProtocoloController(EventoProtocoloService service) {
        this.service = service;
    }

    @GetMapping("/api/protocolos/{protocoloId}/eventos")
    @Operation(
        summary = "Lista eventos do protocolo",
        description = "Retorna eventos ordenados por diasAposInicio. "
            + "Parâmetro opcional ?tipo= filtra por tipo (ex: VACINACAO, RETORNO, EXAME)."
    )
    @ApiResponse(responseCode = "200", description = "Lista de eventos")
    public List<EventoProtocoloResponse> listar(
            @PathVariable Long protocoloId,
            @Parameter(description = "Tipo do evento (ex: VACINACAO, RETORNO, EXAME)")
            @RequestParam(required = false) TipoEventoProtocolo tipo) {
        return service.listarPorProtocolo(protocoloId, tipo);
    }

    @GetMapping("/api/eventos-protocolo/{id}")
    @Operation(summary = "Busca evento de protocolo por id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Evento encontrado"),
        @ApiResponse(responseCode = "404", description = "Evento não encontrado")
    })
    public EventoProtocoloResponse buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping("/api/protocolos/{protocoloId}/eventos")
    @Operation(summary = "Cria evento vinculado ao protocolo")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Evento criado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Protocolo não encontrado")
    })
    public ResponseEntity<EventoProtocoloResponse> criar(
            @PathVariable Long protocoloId,
            @RequestBody @Valid EventoProtocoloRequest request) {
        return ResponseEntity.status(201).body(service.criar(protocoloId, request));
    }

    @PutMapping("/api/eventos-protocolo/{id}")
    @Operation(summary = "Atualiza evento de protocolo")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Evento atualizado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Evento não encontrado")
    })
    public EventoProtocoloResponse atualizar(
            @PathVariable Long id,
            @RequestBody @Valid EventoProtocoloRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/api/eventos-protocolo/{id}")
    @Operation(summary = "Remove evento de protocolo")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Evento removido"),
        @ApiResponse(responseCode = "404", description = "Evento não encontrado")
    })
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
