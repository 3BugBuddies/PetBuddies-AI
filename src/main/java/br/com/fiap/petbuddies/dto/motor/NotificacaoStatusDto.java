package br.com.fiap.petbuddies.dto.motor;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Status do scheduler de notificações")
public class NotificacaoStatusDto {

    @Schema(description = "Data/hora da última execução do scheduler")
    private LocalDateTime ultimaExecucao;

    @Schema(description = "Total de notificações criadas desde o startup")
    private int totalCriadas;

    public NotificacaoStatusDto(LocalDateTime ultimaExecucao, int totalCriadas) {
        this.ultimaExecucao = ultimaExecucao;
        this.totalCriadas = totalCriadas;
    }

    public LocalDateTime getUltimaExecucao() { return ultimaExecucao; }
    public int getTotalCriadas() { return totalCriadas; }
}
