package br.com.fiap.petbuddies.domain.entity;

import br.com.fiap.petbuddies.domain.enums.TipoRisco;
import jakarta.persistence.*;

@Entity
@Table(name = "T_PB_FATOR_RISCO")
public class FatorRiscoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_pb_fator_risco")
    @SequenceGenerator(name = "seq_pb_fator_risco", sequenceName = "SEQ_T_PB_FATOR_RISCO", allocationSize = 1)
    @Column(name = "ID_FATOR_RISCO")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_SCORE_RISCO_ANIMAL", nullable = false)
    private ScoreRiscoAnimalEntity scoreRisco;

    @Enumerated(EnumType.STRING)
    @Column(name = "TP_TIPO", nullable = false)
    private TipoRisco tipo;

    @Column(name = "NR_PESO", nullable = false)
    private int peso;

    @Column(name = "NR_VALOR", nullable = false)
    private int valor;

    @Column(name = "DS_DESCRICAO")
    private String descricao;

    @Column(name = "NR_CONTRIBUICAO")
    private Double contribuicao;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ScoreRiscoAnimalEntity getScoreRisco() { return scoreRisco; }
    public void setScoreRisco(ScoreRiscoAnimalEntity scoreRisco) { this.scoreRisco = scoreRisco; }

    public TipoRisco getTipo() { return tipo; }
    public void setTipo(TipoRisco tipo) { this.tipo = tipo; }

    public int getPeso() { return peso; }
    public void setPeso(int peso) { this.peso = peso; }

    public int getValor() { return valor; }
    public void setValor(int valor) { this.valor = valor; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Double getContribuicao() { return contribuicao; }
    public void setContribuicao(Double contribuicao) { this.contribuicao = contribuicao; }
}
