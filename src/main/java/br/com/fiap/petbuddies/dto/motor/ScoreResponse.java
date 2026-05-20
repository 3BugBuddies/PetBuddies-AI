package br.com.fiap.petbuddies.dto.motor;

import br.com.fiap.petbuddies.domain.entity.ScoreRiscoAnimalEntity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Schema(description = "Score de risco calculado para o animal")
public class ScoreResponse {

    @Schema(description = "ID do registro de score")
    private Long id;

    @Schema(description = "ID do animal no PetBuddies-API (.NET)")
    private Long petNetApiAnimalId;

    @Schema(description = "Valor numérico do score (0–100)")
    private Integer score;

    @Schema(description = "Classificação de risco (BAIXO, MEDIO, ALTO, CRITICO)")
    private String classificacao;

    @Schema(description = "Data/hora do cálculo")
    private LocalDateTime calculadoEm;

    @Schema(description = "Fatores que compuseram o score")
    private List<FatorRiscoDto> fatores;

    @Schema(description = "Score anterior ao recálculo. null nos GETs de histórico")
    private Integer scoreAnterior;

    public static ScoreResponse from(ScoreRiscoAnimalEntity e) {
        return from(e, null);
    }

    public static ScoreResponse from(ScoreRiscoAnimalEntity e, Integer scoreAnterior) {
        ScoreResponse r = new ScoreResponse();
        r.id = e.getId();
        r.petNetApiAnimalId = e.getPetNetApiAnimalId();
        r.score = e.getScore();
        r.classificacao = e.getClassificacao().name();
        r.calculadoEm = e.getCalculadoEm();
        r.fatores = e.getFatores().stream().map(FatorRiscoDto::from).collect(Collectors.toList());
        r.scoreAnterior = scoreAnterior;
        return r;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPetNetApiAnimalId() { return petNetApiAnimalId; }
    public void setPetNetApiAnimalId(Long v) { this.petNetApiAnimalId = v; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public String getClassificacao() { return classificacao; }
    public void setClassificacao(String classificacao) { this.classificacao = classificacao; }

    public LocalDateTime getCalculadoEm() { return calculadoEm; }
    public void setCalculadoEm(LocalDateTime v) { this.calculadoEm = v; }

    public List<FatorRiscoDto> getFatores() { return fatores; }
    public void setFatores(List<FatorRiscoDto> fatores) { this.fatores = fatores; }

    public Integer getScoreAnterior() { return scoreAnterior; }
    public void setScoreAnterior(Integer scoreAnterior) { this.scoreAnterior = scoreAnterior; }
}
