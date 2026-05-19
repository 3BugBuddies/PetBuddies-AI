package br.com.fiap.petbuddies.dto.motor;

import br.com.fiap.petbuddies.domain.entity.FatorRiscoEntity;

public class FatorRiscoDto {

    private String tipo;
    private Integer peso;
    private Integer valor;
    private Double contribuicao;
    private String descricao;

    public static FatorRiscoDto from(FatorRiscoEntity e) {
        FatorRiscoDto dto = new FatorRiscoDto();
        dto.tipo = e.getTipo().name();
        dto.peso = e.getPeso();
        dto.valor = e.getValor();
        dto.contribuicao = e.getContribuicao();
        dto.descricao = e.getDescricao();
        return dto;
    }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Integer getPeso() { return peso; }
    public void setPeso(Integer peso) { this.peso = peso; }

    public Integer getValor() { return valor; }
    public void setValor(Integer valor) { this.valor = valor; }

    public Double getContribuicao() { return contribuicao; }
    public void setContribuicao(Double contribuicao) { this.contribuicao = contribuicao; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
}
