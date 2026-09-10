package br.com.fiap.petbuddies.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

/**
 * Uma condição que o tutor confirmou, vinda do passo de extração — corrigida
 * por ele se a IA errou (§5.1, passo E). Exatamente um dos dois valores é
 * preenchido, espelhando CK_COBS_UM_VALOR.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CondicaoConfirmadaRequest {

    @NotNull(message = "Condição clínica é obrigatória.")
    private Long condicaoClinicaId;

    private Boolean valorBooleano;

    @Digits(integer = 7, fraction = 3, message = "Valor numérico excede a precisão NUMBER(10,3).")
    private BigDecimal valorNumerico;

    @NotNull(message = "Confiança é obrigatória.")
    @DecimalMin(value = "0.0", message = "Confiança deve estar entre 0 e 1.")
    @DecimalMax(value = "1.0", message = "Confiança deve estar entre 0 e 1.")
    private BigDecimal confianca;

    // CK_COBS_UM_VALOR
    @AssertTrue(message = "Informe exatamente um dos valores: booleano ou numérico.")
    private boolean isUmValorPreenchido() {
        return (valorBooleano != null) ^ (valorNumerico != null);
    }
}
