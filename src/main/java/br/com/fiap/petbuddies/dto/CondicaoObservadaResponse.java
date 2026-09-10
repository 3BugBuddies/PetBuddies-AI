package br.com.fiap.petbuddies.dto;

import br.com.fiap.petbuddies.domain.entity.CondicaoObservadaEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CondicaoObservadaResponse {

    private Long id;
    private Long condicaoClinicaId;
    private String codigoCongelado;
    private Boolean valorBooleano;
    private BigDecimal valorNumerico;
    private BigDecimal confianca;
    private LocalDateTime createdAt;

    public static CondicaoObservadaResponse from(CondicaoObservadaEntity entity) {
        CondicaoObservadaResponse dto = new CondicaoObservadaResponse();
        dto.id = entity.getId();
        dto.condicaoClinicaId = entity.getCondicaoClinica() == null ? null : entity.getCondicaoClinica().getId();
        dto.codigoCongelado = entity.getCodigoCongelado();
        dto.valorBooleano = entity.getValorBooleano();
        dto.valorNumerico = entity.getValorNumerico();
        dto.confianca = entity.getConfianca();
        dto.createdAt = entity.getCreatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public Long getCondicaoClinicaId() { return condicaoClinicaId; }
    public String getCodigoCongelado() { return codigoCongelado; }
    public Boolean getValorBooleano() { return valorBooleano; }
    public BigDecimal getValorNumerico() { return valorNumerico; }
    public BigDecimal getConfianca() { return confianca; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
