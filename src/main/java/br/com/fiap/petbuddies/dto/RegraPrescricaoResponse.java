package br.com.fiap.petbuddies.dto;

import br.com.fiap.petbuddies.domain.entity.RegraPrescricaoEntity;
import br.com.fiap.petbuddies.domain.enums.OperadorRegra;
import br.com.fiap.petbuddies.domain.enums.TipoAcaoRegra;
import br.com.fiap.petbuddies.domain.enums.TipoDado;
import br.com.fiap.petbuddies.domain.enums.TipoFonteValor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RegraPrescricaoResponse {

    private Long id;
    private Long prescricaoId;
    private Long condicaoClinicaId;
    private String rotuloCongelado;
    private TipoDado tipoDadoCongelado;
    private TipoFonteValor fonteValorCongelada;
    private OperadorRegra operador;
    private BigDecimal limite;
    private TipoAcaoRegra acaoDose;
    private Integer ordem;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RegraPrescricaoResponse from(RegraPrescricaoEntity entity) {
        RegraPrescricaoResponse dto = new RegraPrescricaoResponse();
        dto.id = entity.getId();
        dto.prescricaoId = entity.getPrescricao() == null ? null : entity.getPrescricao().getId();
        dto.condicaoClinicaId = entity.getCondicaoClinica() == null ? null : entity.getCondicaoClinica().getId();
        dto.rotuloCongelado = entity.getRotuloCongelado();
        dto.tipoDadoCongelado = entity.getTipoDadoCongelado();
        dto.fonteValorCongelada = entity.getFonteValorCongelada();
        dto.operador = entity.getOperador();
        dto.limite = entity.getLimite();
        dto.acaoDose = entity.getAcaoDose();
        dto.ordem = entity.getOrdem();
        dto.createdAt = entity.getCreatedAt();
        dto.updatedAt = entity.getUpdatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public Long getPrescricaoId() { return prescricaoId; }
    public Long getCondicaoClinicaId() { return condicaoClinicaId; }
    public String getRotuloCongelado() { return rotuloCongelado; }
    public TipoDado getTipoDadoCongelado() { return tipoDadoCongelado; }
    public TipoFonteValor getFonteValorCongelada() { return fonteValorCongelada; }
    public OperadorRegra getOperador() { return operador; }
    public BigDecimal getLimite() { return limite; }
    public TipoAcaoRegra getAcaoDose() { return acaoDose; }
    public Integer getOrdem() { return ordem; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
