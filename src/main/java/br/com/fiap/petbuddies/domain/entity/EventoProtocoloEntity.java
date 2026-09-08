package br.com.fiap.petbuddies.domain.entity;

import br.com.fiap.petbuddies.domain.enums.TipoAncora;
import br.com.fiap.petbuddies.domain.enums.TipoEventoProtocolo;
import br.com.fiap.petbuddies.domain.enums.UnidadeTempo;
import jakarta.persistence.*;

/**
 * Molde de um item de cuidado dentro de um protocolo.
 *
 * <p>O agendamento e expresso por ancora + deslocamento + recorrencia, e nao por
 * campos fixos: a ancora diz a data-base, o deslocamento diz quanto somar a ela,
 * e o intervalo com as repeticoes diz quantas vezes o item se repete. Intervalo
 * nulo significa ocorrencia unica.</p>
 */
@Entity
@Table(name = "T_PB_EVENTO_PROTOCOLO")
public class EventoProtocoloEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_EVENTO_PROTOCOLO")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PROTOCOLO", nullable = false)
    private ProtocoloEntity protocolo;

    @Enumerated(EnumType.STRING)
    @Column(name = "TP_TIPO", nullable = false, length = 50)
    private TipoEventoProtocolo tipo;

    @Column(name = "NM_NOME", nullable = false)
    private String nome;

    @Column(name = "NR_OFFSET", nullable = false)
    private int offset;

    @Enumerated(EnumType.STRING)
    @Column(name = "TP_UNIDADE_OFFSET", nullable = false, length = 20)
    private UnidadeTempo unidadeOffset;

    @Enumerated(EnumType.STRING)
    @Column(name = "TP_ANCORA", nullable = false, length = 20)
    private TipoAncora ancora;

    /** Nulo significa ocorrencia unica. Preenchido, exige {@link #unidadeIntervalo}. */
    @Column(name = "NR_INTERVALO")
    private Integer intervalo;

    @Enumerated(EnumType.STRING)
    @Column(name = "TP_UNIDADE_INTERVALO", length = 20)
    private UnidadeTempo unidadeIntervalo;

    /**
     * Quantas vezes o item ocorre, contando a primeira. Obrigatorio: "para sempre"
     * vira numero explicito, porque o plano nao tem fim e um nulo aqui geraria
     * expansao infinita no motor.
     */
    @Column(name = "NR_REPETICOES", nullable = false)
    private Integer repeticoes;

    @Column(name = "DS_DESCRICAO", length = 2000)
    private String descricao;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ProtocoloEntity getProtocolo() { return protocolo; }
    public void setProtocolo(ProtocoloEntity protocolo) { this.protocolo = protocolo; }

    public TipoEventoProtocolo getTipo() { return tipo; }
    public void setTipo(TipoEventoProtocolo tipo) { this.tipo = tipo; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public int getOffset() { return offset; }
    public void setOffset(int offset) { this.offset = offset; }

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
