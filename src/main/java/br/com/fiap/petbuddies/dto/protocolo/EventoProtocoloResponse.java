package br.com.fiap.petbuddies.dto.protocolo;

import br.com.fiap.petbuddies.domain.entity.EventoProtocoloEntity;
import br.com.fiap.petbuddies.domain.enums.TipoEventoProtocolo;

public class EventoProtocoloResponse {

    private Long id;
    private Long protocoloId;
    private TipoEventoProtocolo tipo;
    private String nome;
    private Integer diasAposInicio;
    private Integer mesAplicacao;
    private Integer recorrenciaMeses;
    private String prioridade;
    private String urgencia;
    private String descricao;

    public static EventoProtocoloResponse from(EventoProtocoloEntity entity) {
        EventoProtocoloResponse dto = new EventoProtocoloResponse();
        dto.id = entity.getId();
        dto.protocoloId = entity.getProtocolo().getId();
        dto.tipo = entity.getTipo();
        dto.nome = entity.getNome();
        dto.diasAposInicio = entity.getDiasAposInicio();
        dto.mesAplicacao = entity.getMesAplicacao();
        dto.recorrenciaMeses = entity.getRecorrenciaMeses();
        dto.prioridade = entity.getPrioridade();
        dto.urgencia = entity.getUrgencia();
        dto.descricao = entity.getDescricao();
        return dto;
    }

    public Long getId() { return id; }
    public Long getProtocoloId() { return protocoloId; }
    public TipoEventoProtocolo getTipo() { return tipo; }
    public String getNome() { return nome; }
    public Integer getDiasAposInicio() { return diasAposInicio; }
    public Integer getMesAplicacao() { return mesAplicacao; }
    public Integer getRecorrenciaMeses() { return recorrenciaMeses; }
    public String getPrioridade() { return prioridade; }
    public String getUrgencia() { return urgencia; }
    public String getDescricao() { return descricao; }
}
