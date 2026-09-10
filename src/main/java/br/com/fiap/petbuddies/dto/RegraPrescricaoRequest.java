package br.com.fiap.petbuddies.dto;

import br.com.fiap.petbuddies.domain.enums.prescricao.OperadorRegra;
import br.com.fiap.petbuddies.domain.enums.prescricao.TipoAcaoRegra;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

/**
 * Sem rótulo, tipo de dado ou fonte: os três são congelados no service a
 * partir da condição clínica no momento da criação (ADR s3-10), nunca
 * recebidos do cliente.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegraPrescricaoRequest {

    @NotNull(message = "Prescrição é obrigatória.")
    private Long prescricaoId;

    @NotNull(message = "Condição clínica é obrigatória.")
    private Long condicaoClinicaId;

    /** Obrigatório quando a condição é NUMERICO; deve ser nulo quando é BOOLEANO (CK_REGRA_COERENCIA). */
    private OperadorRegra operador;

    /** Obrigatório quando a condição é NUMERICO; deve ser nulo quando é BOOLEANO (CK_REGRA_COERENCIA). */
    @Digits(integer = 7, fraction = 3, message = "Limite excede a precisão NUMBER(10,3).")
    private BigDecimal limite;

    @NotNull(message = "Ação sobre a dose é obrigatória.")
    private TipoAcaoRegra acaoDose;

    @NotNull(message = "Ordem é obrigatória.")
    @Positive(message = "Ordem deve ser maior que zero.")
    @Max(value = 999, message = "Ordem excede a precisão NUMBER(3).")
    private Integer ordem;
}
