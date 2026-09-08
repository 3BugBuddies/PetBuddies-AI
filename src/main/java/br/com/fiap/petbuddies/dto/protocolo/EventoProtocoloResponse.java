package br.com.fiap.petbuddies.dto.protocolo;

import br.com.fiap.petbuddies.domain.entity.EventoProtocoloEntity;
import br.com.fiap.petbuddies.domain.enums.TipoAncora;
import br.com.fiap.petbuddies.domain.enums.TipoEventoProtocolo;
import br.com.fiap.petbuddies.domain.enums.UnidadeTempo;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Molde de um item de cuidado dentro de um protocolo")
public class EventoProtocoloResponse {

    private Long id;
    private Long protocoloId;
    private TipoEventoProtocolo tipo;
    private String nome;

    @Schema(description = "Quanto somar à âncora para chegar na data do item")
    private Integer offset;

    private UnidadeTempo unidadeOffset;
    private TipoAncora ancora;

    @Schema(description = "Intervalo entre repetições. Nulo significa ocorrência única")
    private Integer intervalo;

    private UnidadeTempo unidadeIntervalo;

    @Schema(description = "Quantas vezes o item ocorre, contando a primeira")
    private Integer repeticoes;

    private String descricao;

    public static EventoProtocoloResponse from(EventoProtocoloEntity entity) {
        EventoProtocoloResponse dto = new EventoProtocoloResponse();
        dto.id = entity.getId();
        dto.protocoloId = entity.getProtocolo() != null ? entity.getProtocolo().getId() : null;
        dto.tipo = entity.getTipo();
        dto.nome = entity.getNome();
        dto.offset = entity.getOffset();
        dto.unidadeOffset = entity.getUnidadeOffset();
        dto.ancora = entity.getAncora();
        dto.intervalo = entity.getIntervalo();
        dto.unidadeIntervalo = entity.getUnidadeIntervalo();
        dto.repeticoes = entity.getRepeticoes();
        dto.descricao = entity.getDescricao();
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProtocoloId() { return protocoloId; }
    public void setProtocoloId(Long protocoloId) { this.protocoloId = protocoloId; }

    public TipoEventoProtocolo getTipo() { return tipo; }
    public void setTipo(TipoEventoProtocolo tipo) { this.tipo = tipo; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Integer getOffset() { return offset; }
    public void setOffset(Integer offset) { this.offset = offset; }

    public UnidadeTempo getUnidadeOffset() { return unidadeOffset; }
    public void setUnidadeOffset(UnidadeTempo unidadeOffset) { this.unidadeOffset = unidadeOffset; }

    public TipoAncora getAncora() { return ancora; }
    public void setAncora(TipoAncora ancora) { this.ancora = ancora; }

    public Integer getIntervalo() { return intervalo; }
    public void setIntervalo(Integer intervalo) { this.intervalo = intervalo; }

    public UnidadeTempo getUnidadeIntervalo() { return unidadeIntervalo; }
    public void setUnidadeIntervalo(UnidadeTempo unidadeIntervalo) { this.unidadeIntervalo = unidadeIntervalo; }

    public Integer getRepeticoes() { return repeticoes; }
    public void setRepeticoes(Integer repeticoes) { this.repeticoes = repeticoes; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
}
