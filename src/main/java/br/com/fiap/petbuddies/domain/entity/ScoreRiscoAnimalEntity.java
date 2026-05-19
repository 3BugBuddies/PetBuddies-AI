package br.com.fiap.petbuddies.domain.entity;

import br.com.fiap.petbuddies.domain.enums.ClassificacaoRisco;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "score_risco_animal")
public class ScoreRiscoAnimalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long petNetApiAnimalId;

    @Column(nullable = false)
    private int score;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClassificacaoRisco classificacao;

    @Column(nullable = false)
    private LocalDateTime calculadoEm;

    @OneToMany(mappedBy = "scoreRisco", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FatorRiscoEntity> fatores = new ArrayList<>();

    @PrePersist
    private void prePersist() {
        calculadoEm = LocalDateTime.now();
    }

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
