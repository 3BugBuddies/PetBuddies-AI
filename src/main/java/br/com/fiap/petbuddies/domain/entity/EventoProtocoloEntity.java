package br.com.fiap.petbuddies.domain.entity;

import br.com.fiap.petbuddies.domain.enums.TipoEventoProtocolo;
import jakarta.persistence.*;

@Entity
@Table(name = "evento_protocolo")
public class EventoProtocoloEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "protocolo_id", nullable = false)
    private ProtocoloEntity protocolo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEventoProtocolo tipo;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private int diasAposInicio;

    @Column
    private Integer mesAplicacao;

    @Column
    private Integer recorrenciaMeses;

    @Column
    private String prioridade;

    @Column
    private String urgencia;

    @Column
    private String descricao;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ProtocoloEntity getProtocolo() { return protocolo; }
    public void setProtocolo(ProtocoloEntity protocolo) { this.protocolo = protocolo; }

    public TipoEventoProtocolo getTipo() { return tipo; }
    public void setTipo(TipoEventoProtocolo tipo) { this.tipo = tipo; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public int getDiasAposInicio() { return diasAposInicio; }
    public void setDiasAposInicio(int diasAposInicio) { this.diasAposInicio = diasAposInicio; }

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
