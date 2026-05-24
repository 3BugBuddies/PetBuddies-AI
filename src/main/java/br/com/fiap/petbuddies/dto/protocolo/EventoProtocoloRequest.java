package br.com.fiap.petbuddies.dto.protocolo;

import br.com.fiap.petbuddies.domain.enums.TipoEventoProtocolo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class EventoProtocoloRequest {

    @NotNull(message = "Tipo é obrigatório.")
    private TipoEventoProtocolo tipo;

    @NotBlank(message = "Nome é obrigatório.")
    private String nome;

    @NotNull(message = "Dias após início é obrigatório.")
    private Integer diasAposInicio;

    private Integer mesAplicacao;
    private Integer recorrenciaMeses;
    private String prioridade;
    private String urgencia;
    private String descricao;

    public TipoEventoProtocolo getTipo() { return tipo; }
    public void setTipo(TipoEventoProtocolo tipo) { this.tipo = tipo; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Integer getDiasAposInicio() { return diasAposInicio; }
    public void setDiasAposInicio(Integer diasAposInicio) { this.diasAposInicio = diasAposInicio; }

    public Integer getMesAplicacao() { return mesAplicacao; }
    public void setMesAplicacao(Integer mesAplicacao) { this.mesAplicacao = mesAplicacao; }

    public Integer getRecorrenciaMeses() { return recorrenciaMeses; }
    public void setRecorrenciaMeses(Integer recorrenciaMeses) { this.recorrenciaMeses = recorrenciaMeses; }

    public String getPrioridade() { return prioridade; }
    public void setPrioridade(String prioridade) { this.prioridade = prioridade; }

    public String getUrgencia() { return urgencia; }
    public void setUrgencia(String urgencia) { this.urgencia = urgencia; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
}
