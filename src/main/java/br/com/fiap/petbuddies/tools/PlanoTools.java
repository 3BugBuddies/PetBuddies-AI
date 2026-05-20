package br.com.fiap.petbuddies.tools;

import br.com.fiap.petbuddies.dto.motor.EventoPlanoDto;
import br.com.fiap.petbuddies.dto.motor.FatorRiscoDto;
import br.com.fiap.petbuddies.dto.motor.PlanoResponse;
import br.com.fiap.petbuddies.dto.motor.ScoreResponse;
import br.com.fiap.petbuddies.service.MotorPlanoService;
import br.com.fiap.petbuddies.service.MotorScoreService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Collectors;

@Component
public class PlanoTools {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final MotorPlanoService motorPlanoService;
    private final MotorScoreService motorScoreService;

    public PlanoTools(MotorPlanoService motorPlanoService, MotorScoreService motorScoreService) {
        this.motorPlanoService = motorPlanoService;
        this.motorScoreService = motorScoreService;
    }

    @Tool(description = "Consulta o plano de cuidados preventivo ativo do animal")
    public String consultarPlanoDoAnimal(Long petNetApiAnimalId) {
        try {
            return motorPlanoService.buscarPlanoAtivo(petNetApiAnimalId)
                    .map(this::formatarPlano)
                    .orElse("VAZIO|nenhum plano ativo encontrado para este animal");
        } catch (Exception e) {
            return "ERRO|não foi possível consultar o plano do animal";
        }
    }

    @Tool(description = "Consulta o score de risco atual do animal com classificação BAIXO/MEDIO/ALTO/CRITICO")
    public String consultarScoreDoAnimal(Long petNetApiAnimalId) {
        try {
            ScoreResponse score = motorScoreService.buscarMaisRecente(petNetApiAnimalId)
                    .orElseGet(() -> motorScoreService.recalcular(petNetApiAnimalId, "CONSULTA_PLANO"));
            String fatores = score.getFatores() == null ? "" : score.getFatores().stream()
                    .filter(f -> f.getValor() != null && f.getValor() > 0)
                    .limit(3)
                    .map(FatorRiscoDto::getDescricao)
                    .collect(Collectors.joining("; "));
            return "OK|score=" + score.getScore()
                    + "|classificacao=" + score.getClassificacao()
                    + (fatores.isBlank() ? "" : "|fatores=" + fatores);
        } catch (Exception e) {
            return "ERRO|não foi possível consultar o score do animal";
        }
    }

    private String formatarPlano(PlanoResponse plano) {
        EventoPlanoDto proximo = plano.getEventos() == null ? null
                : plano.getEventos().stream()
                        .filter(e -> "PENDENTE".equals(e.getStatus()))
                        .filter(e -> e.getDataAlvo() == null || !e.getDataAlvo().isBefore(LocalDate.now()))
                        .min(Comparator.comparing(EventoPlanoDto::getDataAlvo,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                        .orElse(null);
        StringBuilder sb = new StringBuilder("OK|plano=").append(plano.getProtocoloNome())
                .append("|status=").append(plano.getStatus());
        if (plano.getScoreAtual() != null) sb.append("|score=").append(plano.getScoreAtual());
        if (proximo != null) sb.append("|proximoEvento=").append(proximo.getNome())
                .append("|dataAlvo=").append(proximo.getDataAlvo() == null
                        ? "sem data" : DATA.format(proximo.getDataAlvo()));
        return sb.toString();
    }
}
