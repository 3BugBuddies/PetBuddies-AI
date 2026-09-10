package br.com.fiap.petbuddies.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Uma prescrição dentro do fechamento — o ato assinado, com a orientação e
 * as regras condicionais no mesmo objeto. Sem animalId, veterinarioId nem
 * registroAtendimentoId: vêm do atendimento que está sendo fechado, não do
 * corpo.
 */
public class PrescricaoFechamentoRequest {

    @NotBlank(message = "Medicamento é obrigatório.")
    @Size(max = 150, message = "Medicamento deve ter no máximo 150 caracteres.")
    private String medicamento;

    @NotNull(message = "Dose mínima é obrigatória.")
    @Digits(integer = 5, fraction = 3, message = "Dose mínima excede a precisão NUMBER(8,3).")
    private BigDecimal doseMin;

    @NotNull(message = "Dose máxima é obrigatória.")
    @Digits(integer = 5, fraction = 3, message = "Dose máxima excede a precisão NUMBER(8,3).")
    private BigDecimal doseMax;

    @NotBlank(message = "Unidade da dose é obrigatória.")
    @Size(max = 20, message = "Unidade deve ter no máximo 20 caracteres.")
    private String unidade;

    @NotNull(message = "Frequência diária é obrigatória.")
    @Positive(message = "Frequência diária deve ser maior que zero.")
    @Max(value = 99, message = "Frequência diária excede a precisão NUMBER(2).")
    private Integer frequenciaDia;

    @NotNull(message = "Duração em dias é obrigatória.")
    @Positive(message = "Duração em dias deve ser maior que zero.")
    @Max(value = 9999, message = "Duração em dias excede a precisão NUMBER(4).")
    private Integer duracaoDias;

    @NotNull(message = "Data de início é obrigatória.")
    private LocalDate dataInicio;

    private String orientacao;

    private Long materialOrigemId;

    private Integer versaoOrigem;

    @Valid
    private List<RegraPrescricaoFechamentoRequest> regras;

    // CK_PRESCRICAO_FAIXA: a faixa invertida quebraria a função de dose antes
    // de qualquer regra rodar.
    @AssertTrue(message = "Dose mínima não pode ser maior que a dose máxima.")
    private boolean isFaixaDoseValida() {
        return doseMin == null || doseMax == null || doseMin.compareTo(doseMax) <= 0;
    }

    public String getMedicamento() { return medicamento; }
    public void setMedicamento(String medicamento) { this.medicamento = medicamento; }

    public BigDecimal getDoseMin() { return doseMin; }
    public void setDoseMin(BigDecimal doseMin) { this.doseMin = doseMin; }

    public BigDecimal getDoseMax() { return doseMax; }
    public void setDoseMax(BigDecimal doseMax) { this.doseMax = doseMax; }

    public String getUnidade() { return unidade; }
    public void setUnidade(String unidade) { this.unidade = unidade; }

    public Integer getFrequenciaDia() { return frequenciaDia; }
    public void setFrequenciaDia(Integer frequenciaDia) { this.frequenciaDia = frequenciaDia; }

    public Integer getDuracaoDias() { return duracaoDias; }
    public void setDuracaoDias(Integer duracaoDias) { this.duracaoDias = duracaoDias; }

    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }

    public String getOrientacao() { return orientacao; }
    public void setOrientacao(String orientacao) { this.orientacao = orientacao; }

    public Long getMaterialOrigemId() { return materialOrigemId; }
    public void setMaterialOrigemId(Long materialOrigemId) { this.materialOrigemId = materialOrigemId; }

    public Integer getVersaoOrigem() { return versaoOrigem; }
    public void setVersaoOrigem(Integer versaoOrigem) { this.versaoOrigem = versaoOrigem; }

    public List<RegraPrescricaoFechamentoRequest> getRegras() { return regras; }
    public void setRegras(List<RegraPrescricaoFechamentoRequest> regras) { this.regras = regras; }
}
