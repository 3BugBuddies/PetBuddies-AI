package br.com.fiap.petbuddies.dto.motor;

import br.com.fiap.petbuddies.domain.entity.FatorRiscoEntity;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Fator de risco que contribuiu para o score")
public class FatorRiscoDto {

    @Schema(description = "Tipo do fator (IDADE, CASTRADO, CONDICAO_CRONICA, FREQUENCIA_CONSULTAS)")
    private String tipo;

    @Schema(description = "Peso do fator no cálculo (0–10)")
    private Integer peso;

    @Schema(description = "Valor bruto detectado para o fator")
    private Integer valor;

    @Schema(description = "Contribuição percentual do fator no score final")
    private Double contribuicao;

    @Schema(description = "Descrição textual do fator")
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
