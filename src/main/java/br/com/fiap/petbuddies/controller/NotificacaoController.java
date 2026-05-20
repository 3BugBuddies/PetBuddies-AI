package br.com.fiap.petbuddies.controller;

import br.com.fiap.petbuddies.dto.motor.NotificacaoStatusDto;
import br.com.fiap.petbuddies.service.NotificacaoSchedulerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/motor/notificacoes")
@Tag(name = "motor — notificações", description = "Health do scheduler de notificações de eventos do plano")
public class NotificacaoController {

    private final NotificacaoSchedulerService schedulerService;

    public NotificacaoController(NotificacaoSchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    @GetMapping("/status")
    @Operation(summary = "Status do scheduler de notificações",
               description = "Retorna última execução e total de notificações criadas desde o startup.")
    @ApiResponse(responseCode = "200", description = "Status retornado")
    public ResponseEntity<NotificacaoStatusDto> status() {
        return ResponseEntity.ok(new NotificacaoStatusDto(
                schedulerService.getUltimaExecucao(),
                schedulerService.getTotalCriadas()));
    }
}
