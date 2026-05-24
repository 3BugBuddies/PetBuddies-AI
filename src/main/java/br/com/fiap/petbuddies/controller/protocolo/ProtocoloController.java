package br.com.fiap.petbuddies.controller.protocolo;

import br.com.fiap.petbuddies.domain.enums.CategoriaProtocolo;
import br.com.fiap.petbuddies.domain.enums.Especie;
import br.com.fiap.petbuddies.dto.protocolo.ProtocoloRequest;
import br.com.fiap.petbuddies.dto.protocolo.ProtocoloResponse;
import br.com.fiap.petbuddies.service.protocolo.ProtocoloService;
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
@RequestMapping("/api/protocolos")
@Tag(name = "catalogo — protocolos", description = "CRUD e buscas customizadas de protocolos de cuidado")
public class ProtocoloController {

    private final ProtocoloService service;

    public ProtocoloController(ProtocoloService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista protocolos ativos", description = "Retorna todos os protocolos com ativo=true.")
    @ApiResponse(responseCode = "200", description = "Lista de protocolos")
    public List<ProtocoloResponse> listar() {
        return service.listarAtivos();
    }

    @GetMapping("/buscar")
    @Operation(
        summary = "Busca protocolos por categoria e/ou espécie",
        description = "Filtra protocolos ativos. Se categoria e espécie forem informadas, aplica filtro combinado (query method). "
            + "Se apenas categoria for informada, filtra só por categoria. Sem parâmetros equivale a listar todos ativos."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Protocolos encontrados")
    })
    public List<ProtocoloResponse> buscar(
            @Parameter(description = "Categoria do protocolo (ex: PREVENTIVO, POS_CIRURGICO)")
            @RequestParam(required = false) CategoriaProtocolo categoria,
            @Parameter(description = "Espécie do animal (ex: CACHORRO, GATO)")
            @RequestParam(required = false) Especie especie) {
        return service.buscar(categoria, especie);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca protocolo por id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Protocolo encontrado"),
        @ApiResponse(responseCode = "404", description = "Protocolo não encontrado")
    })
    public ProtocoloResponse buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    @Operation(summary = "Cria protocolo", description = "Cria um novo protocolo de cuidado. Retorna 201 com o protocolo criado.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Protocolo criado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<ProtocoloResponse> criar(@RequestBody @Valid ProtocoloRequest request) {
        return ResponseEntity.status(201).body(service.criar(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza protocolo")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Protocolo atualizado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Protocolo não encontrado")
    })
    public ProtocoloResponse atualizar(@PathVariable Long id, @RequestBody @Valid ProtocoloRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove protocolo")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Protocolo removido"),
        @ApiResponse(responseCode = "404", description = "Protocolo não encontrado")
    })
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
