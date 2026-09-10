package br.com.fiap.petbuddies.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrescricaoRequest {

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

    /** Aponta para o bulário, que só existe na Sprint 4. */
    private Long materialOrigemId;

    private Integer versaoOrigem;

    @NotNull(message = "Animal é obrigatório.")
    private Long animalId;

    @NotNull(message = "Veterinário é obrigatório.")
    private Long veterinarioId;

    @NotNull(message = "Registro de atendimento é obrigatório.")
    private Long registroAtendimentoId;

    // CK_PRESCRICAO_FAIXA: a faixa invertida quebraria a função de dose antes
    // de qualquer regra rodar.
    @AssertTrue(message = "Dose mínima não pode ser maior que a dose máxima.")
    private boolean isFaixaDoseValida() {
        return doseMin == null || doseMax == null || doseMin.compareTo(doseMax) <= 0;
    }
}
