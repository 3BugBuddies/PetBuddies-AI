package br.com.fiap.petbuddies.controller;

import br.com.fiap.petbuddies.dto.cuidado.ItemPlanoCuidadoDto;
import br.com.fiap.petbuddies.dto.cuidado.PlanoPreventivoRequest;
import br.com.fiap.petbuddies.dto.cuidado.PlanoPosCirurgicoRequest;
import br.com.fiap.petbuddies.dto.cuidado.PlanoResponse;
import br.com.fiap.petbuddies.dto.cuidado.ProtocoloAplicadoResponse;
import br.com.fiap.petbuddies.dto.cuidado.SugestaoCuidadoDto;
import br.com.fiap.petbuddies.assembler.PlanoModelAssembler;
import br.com.fiap.petbuddies.assembler.ProtocoloAplicadoModelAssembler;
import br.com.fiap.petbuddies.assembler.SugestaoCuidadoModelAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import br.com.fiap.petbuddies.service.MotorPlanoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/motor/planos")
@Tag(name = "motor — planos", description = "Instanciação e consulta de planos de cuidado preventivo e pós-cirúrgico")
public class MotorPlanoController {

    private final MotorPlanoService motorPlanoService;
    private final PlanoModelAssembler assembler;
    private final ProtocoloAplicadoModelAssembler protocoloAplicadoAssembler;
    private final SugestaoCuidadoModelAssembler sugestaoAssembler;

    public MotorPlanoController(MotorPlanoService motorPlanoService, PlanoModelAssembler assembler,
                                 ProtocoloAplicadoModelAssembler protocoloAplicadoAssembler,
                                 SugestaoCuidadoModelAssembler sugestaoAssembler) {
        this.motorPlanoService = motorPlanoService;
        this.assembler = assembler;
        this.protocoloAplicadoAssembler = protocoloAplicadoAssembler;
        this.sugestaoAssembler = sugestaoAssembler;
    }

    @PostMapping("/instanciar-preventivo")
    @Operation(
        summary = "Instanciar plano preventivo",
        description = "Cria um plano preventivo para o animal com base no protocolo mais específico disponível. "
            + "Idempotente: retorna o plano ATIVO existente se já houver um (status 200). "
            + "Retorna status 201 quando cria novo plano, 200 quando já existia. "
            + "Se não houver protocolo compatível, retorna 200 com criado=false e motivo=SEM_PROTOCOLO_COMPATIVEL."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Plano criado com sucesso"),
        @ApiResponse(responseCode = "200", description = "Plano já existia (idempotência) ou nenhum protocolo compatível"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos — animalId, especie ou dataNascimento ausentes")
    })
    public ResponseEntity<EntityModel<PlanoResponse>> instanciarPlanoPreventivo(@RequestBody @Valid PlanoPreventivoRequest req) {
        PlanoResponse response = motorPlanoService.instanciarPreventivo(req);
        int status = Boolean.TRUE.equals(response.getCriado()) ? 201 : 200;
        return ResponseEntity.status(status).body(assembler.toModel(response));
    }

    @PostMapping("/instanciar-pos-cirurgico")
    @Operation(
        summary = "Instanciar plano pós-cirúrgico",
        description = "Cria um plano de recuperação pós-cirúrgica vinculado a uma consulta. "
            + "Idempotente por (animalId + consultaId): retorna o plano existente se já houver um (status 200)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Plano pós-cirúrgico criado com sucesso"),
        @ApiResponse(responseCode = "200", description = "Plano já existia para esta consulta (idempotência)"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<EntityModel<PlanoResponse>> instanciarPlanoPosCirurgico(@RequestBody @Valid PlanoPosCirurgicoRequest req) {
        PlanoResponse response = motorPlanoService.instanciarPosCirurgico(req);
        int status = Boolean.TRUE.equals(response.getCriado()) ? 201 : 200;
        return ResponseEntity.status(status).body(assembler.toModel(response));
    }

    @GetMapping("/{animalId}")
    @Operation(summary = "Buscar plano ativo", description = "Retorna o plano de cuidado ATIVO do animal com seus eventos pendentes.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Plano encontrado"),
        @ApiResponse(responseCode = "404", description = "Nenhum plano ativo para este animal")
    })
    public ResponseEntity<EntityModel<PlanoResponse>> buscarPlano(
            @Parameter(description = "ID do animal no PetBuddies-API (.NET)") @PathVariable Long animalId) {
        return motorPlanoService.buscarPlanoAtivo(animalId)
                .map(plano -> ResponseEntity.ok(assembler.toModel(plano)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{animalId}/eventos")
    @Operation(summary = "Listar eventos do plano", description = "Lista paginada dos eventos do plano ativo do animal. Use ?page=0&size=10.")
    @ApiResponse(responseCode = "200", description = "Lista de eventos")
    public Page<ItemPlanoCuidadoDto> listarEventos(
            @Parameter(description = "ID do animal no PetBuddies-API (.NET)") @PathVariable Long animalId,
            @ParameterObject Pageable pageable) {
        return motorPlanoService.listarEventos(animalId, pageable);
    }

    @GetMapping("/{animalId}/protocolo-aplicado")
    @Operation(
        summary = "Protocolo aplicado",
        description = "O que o protocolo já produziu no animal: os planos nascidos de molde do catálogo, "
            + "com os itens separados em realizados, pendentes e vencidos. Leitura 100% local — não depende "
            + "do catálogo do .NET estar no ar. Lista vazia quando o animal não tem plano com protocolo."
    )
    @ApiResponse(responseCode = "200", description = "Lista de planos com protocolo, mesmo vazia")
    public CollectionModel<EntityModel<ProtocoloAplicadoResponse>> protocoloAplicado(
            @Parameter(description = "ID do animal no PetBuddies-API (.NET)") @PathVariable Long animalId) {
        return protocoloAplicadoAssembler.toCollectionModel(motorPlanoService.buscarProtocoloAplicado(animalId));
    }

    @GetMapping("/{animalId}/sugestoes")
    @Operation(
        summary = "Sugestão por histórico",
        description = "Próximo cuidado a partir do que o animal já recebeu: reforço vencido (leitura local), "
            + "recorrência devida e nunca-realizado (dependem do catálogo do .NET — somem quando ele está fora do ar). "
            + "Lista vazia quando não há nada a sugerir."
    )
    @ApiResponse(responseCode = "200", description = "Lista de sugestões, mesmo vazia")
    public CollectionModel<EntityModel<SugestaoCuidadoDto>> sugestoes(
            @Parameter(description = "ID do animal no PetBuddies-API (.NET)") @PathVariable Long animalId) {
        return sugestaoAssembler.toCollectionModel(motorPlanoService.sugerirPorHistorico(animalId));
    }
}
