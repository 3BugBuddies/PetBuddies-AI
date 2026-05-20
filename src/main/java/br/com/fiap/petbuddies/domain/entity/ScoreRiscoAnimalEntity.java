package br.com.fiap.petbuddies.domain.entity;

import br.com.fiap.petbuddies.domain.enums.ClassificacaoRisco;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "T_PB_SCORE_RISCO_ANIMAL")
public class ScoreRiscoAnimalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_pb_score")
    @SequenceGenerator(name = "seq_pb_score", sequenceName = "SEQ_T_PB_SCORE", allocationSize = 1)
    @Column(name = "ID_SCORE_RISCO_ANIMAL")
    private Long id;

    @Column(name = "ID_PET_NET_ANIMAL", nullable = false)
    private Long petNetApiAnimalId;

    @Column(name = "NR_SCORE", nullable = false)
    private int score;

    @Enumerated(EnumType.STRING)
    @Column(name = "CS_CLASSIFICACAO", nullable = false)
    private ClassificacaoRisco classificacao;

    @Column(name = "DT_CALCULADO_EM", nullable = false)
    private LocalDateTime calculadoEm;

    @OneToMany(mappedBy = "scoreRisco", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FatorRiscoEntity> fatores = new ArrayList<>();

    @PrePersist
    private void prePersist() { calculadoEm = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPetNetApiAnimalId() { return petNetApiAnimalId; }
    public void setPetNetApiAnimalId(Long petNetApiAnimalId) { this.petNetApiAnimalId = petNetApiAnimalId; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public ClassificacaoRisco getClassificacao() { return classificacao; }
    public void setClassificacao(ClassificacaoRisco classificacao) { this.classificacao = classificacao; }

    public LocalDateTime getCalculadoEm() { return calculadoEm; }
    public void setCalculadoEm(LocalDateTime calculadoEm) { this.calculadoEm = calculadoEm; }

    public List<FatorRiscoEntity> getFatores() { return fatores; }
    public void setFatores(List<FatorRiscoEntity> fatores) { this.fatores = fatores; }
}
