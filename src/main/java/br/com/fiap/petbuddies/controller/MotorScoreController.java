package br.com.fiap.petbuddies.controller;

import br.com.fiap.petbuddies.dto.motor.RecalcularScoreRequest;
import br.com.fiap.petbuddies.dto.motor.ScoreResponse;
import br.com.fiap.petbuddies.service.MotorScoreService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/motor/scores")
@Tag(name = "motor")
public class MotorScoreController {

    private final MotorScoreService motorScoreService;

    public MotorScoreController(MotorScoreService motorScoreService) {
        this.motorScoreService = motorScoreService;
    }

    @PostMapping("/recalcular")
    public ScoreResponse recalcular(@RequestBody @Valid RecalcularScoreRequest req) {
        return motorScoreService.recalcular(req.getPetNetApiAnimalId(), req.getMotivo());
    }

    @GetMapping("/{petNetApiAnimalId}")
    public ResponseEntity<ScoreResponse> buscarScore(@PathVariable Long petNetApiAnimalId) {
        return motorScoreService.buscarMaisRecente(petNetApiAnimalId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{petNetApiAnimalId}/historico")
    public Page<ScoreResponse> historico(
            @PathVariable Long petNetApiAnimalId, Pageable pageable) {
        return motorScoreService.listarHistorico(petNetApiAnimalId, pageable);
    }
}
