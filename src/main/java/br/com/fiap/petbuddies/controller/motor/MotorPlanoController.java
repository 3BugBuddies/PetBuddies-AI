package br.com.fiap.petbuddies.controller.motor;

import br.com.fiap.petbuddies.dto.motor.EventoPlanoDto;
import br.com.fiap.petbuddies.dto.motor.PlanoPreventivoRequest;
import br.com.fiap.petbuddies.dto.motor.PlanoPosCirurgicoRequest;
import br.com.fiap.petbuddies.dto.motor.PlanoResponse;
import br.com.fiap.petbuddies.service.motor.MotorPlanoService;
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

    public MotorPlanoController(MotorPlanoService motorPlanoService) {
        this.motorPlanoService = motorPlanoService;
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
        @ApiResponse(responseCode = "400", description = "Dados inválidos — petNetApiAnimalId, especie ou dataNascimento ausentes")
    })
    public ResponseEntity<PlanoResponse> instanciarPlanoPreventivo(@RequestBody @Valid PlanoPreventivoRequest req) {
        PlanoResponse response = motorPlanoService.instanciarPreventivo(req);
        int status = Boolean.TRUE.equals(response.getCriado()) ? 201 : 200;
        return ResponseEntity.status(status).body(response);
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
    public ResponseEntity<PlanoResponse> instanciarPlanoPosCirurgico(@RequestBody @Valid PlanoPosCirurgicoRequest req) {
        PlanoResponse response = motorPlanoService.instanciarPosCirurgico(req);
        int status = Boolean.TRUE.equals(response.getCriado()) ? 201 : 200;
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/{petNetApiAnimalId}")
    @Operation(summary = "Buscar plano ativo", description = "Retorna o plano de cuidado ATIVO do animal com seus eventos pendentes.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Plano encontrado"),
        @ApiResponse(responseCode = "404", description = "Nenhum plano ativo para este animal")
    })
    public ResponseEntity<PlanoResponse> buscarPlano(
            @Parameter(description = "ID do animal no PetBuddies-API (.NET)") @PathVariable Long petNetApiAnimalId) {
        return motorPlanoService.buscarPlanoAtivo(petNetApiAnimalId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{petNetApiAnimalId}/eventos")
    @Operation(summary = "Listar eventos do plano", description = "Lista paginada dos eventos do plano ativo do animal. Use ?page=0&size=10.")
    @ApiResponse(responseCode = "200", description = "Lista de eventos")
    public Page<EventoPlanoDto> listarEventos(
            @Parameter(description = "ID do animal no PetBuddies-API (.NET)") @PathVariable Long petNetApiAnimalId,
            @ParameterObject Pageable pageable) {
        return motorPlanoService.listarEventos(petNetApiAnimalId, pageable);
    }
}
