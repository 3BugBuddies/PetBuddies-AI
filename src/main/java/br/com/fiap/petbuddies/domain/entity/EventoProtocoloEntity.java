package br.com.fiap.petbuddies.domain.entity;

import br.com.fiap.petbuddies.domain.enums.TipoEventoProtocolo;
import jakarta.persistence.*;

@Entity
@Table(name = "T_PB_EVENTO_PROTOCOLO")
public class EventoProtocoloEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_pb_evento_prot")
    @SequenceGenerator(name = "seq_pb_evento_prot", sequenceName = "SEQ_T_PB_EVENTO_PROT", allocationSize = 1)
    @Column(name = "ID_EVENTO_PROTOCOLO")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PROTOCOLO", nullable = false)
    private ProtocoloEntity protocolo;

    @Enumerated(EnumType.STRING)
    @Column(name = "TP_TIPO", nullable = false)
    private TipoEventoProtocolo tipo;

    @Column(name = "NM_NOME", nullable = false)
    private String nome;

    @Column(name = "NR_DIAS_APOS_INICIO", nullable = false)
    private int diasAposInicio;

    @Column(name = "NR_MES_APLICACAO")
    private Integer mesAplicacao;

    @Column(name = "NR_RECORRENCIA_MESES")
    private Integer recorrenciaMeses;

    @Column(name = "ST_PRIORIDADE")
    private String prioridade;

    @Column(name = "ST_URGENCIA")
    private String urgencia;

    @Column(name = "DS_DESCRICAO")
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
