package br.com.fiap.petbuddies.domain.entity;

import br.com.fiap.petbuddies.domain.enums.TipoRisco;
import jakarta.persistence.*;

@Entity
@Table(name = "fator_risco")
public class FatorRiscoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "score_risco_id", nullable = false)
    private ScoreRiscoAnimalEntity scoreRisco;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoRisco tipo;

    @Column(nullable = false)
    private int peso;

    @Column(nullable = false)
    private int valor;

    private String descricao;

    @Column
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
