package br.com.fiap.petbuddies.dto;

import br.com.fiap.petbuddies.domain.enums.TipoDado;

import java.math.BigDecimal;

/**
 * Uma condição que a IA reconheceu na narrativa, já resolvida contra o
 * catálogo (contrato §9.1). Ausência desta condição na lista devolvida por
 * {@link CheckinExtracaoResponse} é o nulo explícito — o tutor não falou
 * nela. Presente com {@code valorBooleano=false} é "mencionada e negada": os
 * dois nunca colapsam no mesmo valor.
 */
public class CondicaoExtraidaResponse {

    private Long condicaoClinicaId;
    private String codigo;
    private String rotulo;
    private TipoDado tipoDado;
    private String unidade;
    private Boolean valorBooleano;
    private BigDecimal valorNumerico;
    private Double confianca;
    private boolean critica;

    public static CondicaoExtraidaResponse of(
            Long condicaoClinicaId, String codigo, String rotulo, TipoDado tipoDado, String unidade,
            Boolean valorBooleano, BigDecimal valorNumerico, Double confianca, boolean critica) {
        CondicaoExtraidaResponse dto = new CondicaoExtraidaResponse();
        dto.condicaoClinicaId = condicaoClinicaId;
        dto.codigo = codigo;
        dto.rotulo = rotulo;
        dto.tipoDado = tipoDado;
        dto.unidade = unidade;
        dto.valorBooleano = valorBooleano;
        dto.valorNumerico = valorNumerico;
        dto.confianca = confianca;
        dto.critica = critica;
        return dto;
    }

    public Long getCondicaoClinicaId() { return condicaoClinicaId; }
    public String getCodigo() { return codigo; }
    public String getRotulo() { return rotulo; }
    public TipoDado getTipoDado() { return tipoDado; }
    public String getUnidade() { return unidade; }
    public Boolean getValorBooleano() { return valorBooleano; }
    public BigDecimal getValorNumerico() { return valorNumerico; }
    public Double getConfianca() { return confianca; }
    public boolean isCritica() { return critica; }
}
