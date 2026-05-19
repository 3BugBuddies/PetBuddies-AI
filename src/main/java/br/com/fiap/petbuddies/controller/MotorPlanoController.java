package br.com.fiap.petbuddies.controller;

import br.com.fiap.petbuddies.dto.motor.EventoPlanoDto;
import br.com.fiap.petbuddies.dto.motor.PlanoPreventivoRequest;
import br.com.fiap.petbuddies.dto.motor.PlanoPosCirurgicoRequest;
import br.com.fiap.petbuddies.dto.motor.PlanoResponse;
import br.com.fiap.petbuddies.service.MotorPlanoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/motor/planos")
@Tag(name = "motor")
public class MotorPlanoController {

    private final MotorPlanoService motorPlanoService;

    public MotorPlanoController(MotorPlanoService motorPlanoService) {
        this.motorPlanoService = motorPlanoService;
    }

    @PostMapping("/instanciar")
    public ResponseEntity<PlanoResponse> instanciar(@RequestBody @Valid PlanoPreventivoRequest req) {
        PlanoResponse response = motorPlanoService.instanciarPreventivo(req);
        int status = Boolean.TRUE.equals(response.getCriado()) ? 201 : 200;
        return ResponseEntity.status(status).body(response);
    }

    @PostMapping("/pos-cirurgico")
    public ResponseEntity<PlanoResponse> posCirurgico(@RequestBody @Valid PlanoPosCirurgicoRequest req) {
        PlanoResponse response = motorPlanoService.instanciarPosCirurgico(req);
        int status = Boolean.TRUE.equals(response.getCriado()) ? 201 : 200;
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/{petNetApiAnimalId}")
    public ResponseEntity<PlanoResponse> buscarPlano(@PathVariable Long petNetApiAnimalId) {
        return motorPlanoService.buscarPlanoAtivo(petNetApiAnimalId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{petNetApiAnimalId}/eventos")
    public Page<EventoPlanoDto> listarEventos(
            @PathVariable Long petNetApiAnimalId, Pageable pageable) {
        return motorPlanoService.listarEventos(petNetApiAnimalId, pageable);
    }
}
