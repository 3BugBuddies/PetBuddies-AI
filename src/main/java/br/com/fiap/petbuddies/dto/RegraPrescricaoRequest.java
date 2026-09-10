package br.com.fiap.petbuddies.dto;

import br.com.fiap.petbuddies.domain.enums.OperadorRegra;
import br.com.fiap.petbuddies.domain.enums.TipoAcaoRegra;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Sem rótulo, tipo de dado ou fonte: os três são congelados no service a
 * partir da condição clínica no momento da criação (ADR s3-10), nunca
 * recebidos do cliente.
 */
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

    public Long getPrescricaoId() { return prescricaoId; }
    public void setPrescricaoId(Long prescricaoId) { this.prescricaoId = prescricaoId; }

    public Long getCondicaoClinicaId() { return condicaoClinicaId; }
    public void setCondicaoClinicaId(Long condicaoClinicaId) { this.condicaoClinicaId = condicaoClinicaId; }

    public OperadorRegra getOperador() { return operador; }
    public void setOperador(OperadorRegra operador) { this.operador = operador; }

    public BigDecimal getLimite() { return limite; }
    public void setLimite(BigDecimal limite) { this.limite = limite; }

    public TipoAcaoRegra getAcaoDose() { return acaoDose; }
    public void setAcaoDose(TipoAcaoRegra acaoDose) { this.acaoDose = acaoDose; }

    public Integer getOrdem() { return ordem; }
    public void setOrdem(Integer ordem) { this.ordem = ordem; }
}
