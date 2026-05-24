package br.com.fiap.petbuddies.controller.motor;

import br.com.fiap.petbuddies.dto.motor.RecalcularScoreRequest;
import br.com.fiap.petbuddies.dto.motor.ScoreResponse;
import br.com.fiap.petbuddies.service.motor.MotorScoreService;
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
@RequestMapping("/api/motor/scores")
@Tag(name = "motor — scores", description = "Cálculo e histórico de scores de risco por animal")
public class MotorScoreController {

    private final MotorScoreService motorScoreService;

    public MotorScoreController(MotorScoreService motorScoreService) {
        this.motorScoreService = motorScoreService;
    }

    @PostMapping("/recalcular")
    @Operation(
        summary = "Recalcular score de risco",
        description = "Recalcula e persiste o score de risco do animal com base em fatores clínicos "
            + "(idade, castrado, condição crônica, frequência de consultas). "
            + "Cada chamada gera um novo registro de histórico."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Score calculado e persistido"),
        @ApiResponse(responseCode = "400", description = "petNetApiAnimalId ausente ou inválido"),
        @ApiResponse(responseCode = "404", description = "Animal não encontrado no PetBuddies-API")
    })
    public ScoreResponse recalcular(@RequestBody @Valid RecalcularScoreRequest req) {
        return motorScoreService.recalcular(req.getPetNetApiAnimalId(), req.getMotivo());
    }

    @GetMapping("/{petNetApiAnimalId}")
    @Operation(summary = "Buscar score mais recente", description = "Retorna o último score de risco calculado para o animal.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Score encontrado"),
        @ApiResponse(responseCode = "404", description = "Nenhum score calculado para este animal")
    })
    public ResponseEntity<ScoreResponse> buscarScore(
            @Parameter(description = "ID do animal no PetBuddies-API (.NET)") @PathVariable Long petNetApiAnimalId) {
        return motorScoreService.buscarMaisRecente(petNetApiAnimalId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{petNetApiAnimalId}/historico")
    @Operation(summary = "Histórico de scores", description = "Lista paginada de todos os scores calculados para o animal, mais recente primeiro. Use ?page=0&size=10.")
    @ApiResponse(responseCode = "200", description = "Histórico de scores")
    public Page<ScoreResponse> historico(
            @Parameter(description = "ID do animal no PetBuddies-API (.NET)") @PathVariable Long petNetApiAnimalId,
            @ParameterObject Pageable pageable) {
        return motorScoreService.listarHistorico(petNetApiAnimalId, pageable);
    }
}
