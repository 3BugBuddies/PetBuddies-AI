package br.com.fiap.petbuddies.domain.entity;

import br.com.fiap.petbuddies.domain.enums.StatusPlano;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "plano_cuidado_animal")
public class PlanoCuidadoAnimalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long petNetApiAnimalId;

    @Column
    private Long petNetApiConsultaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "protocolo_id", nullable = false)
    private ProtocoloEntity protocolo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPlano status = StatusPlano.ATIVO;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    private LocalDateTime atualizadoEm;

    @Column
    private Integer scoreAtual;

    @Column
    private LocalDateTime ultimoRecalculo;

    @Column
    private LocalDateTime canceladoEm;

    @Column
    private String motivoCancelamento;

    @OneToMany(mappedBy = "plano", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EventoPlanoEntity> eventos = new ArrayList<>();

    @PrePersist
    private void prePersist() {
        criadoEm = LocalDateTime.now();
    }

    @PreUpdate
    private void preUpdate() {
        atualizadoEm = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPetNetApiAnimalId() { return petNetApiAnimalId; }
    public void setPetNetApiAnimalId(Long petNetApiAnimalId) { this.petNetApiAnimalId = petNetApiAnimalId; }

    public Long getPetNetApiConsultaId() { return petNetApiConsultaId; }
    public void setPetNetApiConsultaId(Long petNetApiConsultaId) { this.petNetApiConsultaId = petNetApiConsultaId; }

    public ProtocoloEntity getProtocolo() { return protocolo; }
    public void setProtocolo(ProtocoloEntity protocolo) { this.protocolo = protocolo; }

    public StatusPlano getStatus() { return status; }
    public void setStatus(StatusPlano status) { this.status = status; }

    public LocalDateTime getCriadoEm() { return criadoEm; }

    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }

    public Integer getScoreAtual() { return scoreAtual; }
    public void setScoreAtual(Integer scoreAtual) { this.scoreAtual = scoreAtual; }

    public LocalDateTime getUltimoRecalculo() { return ultimoRecalculo; }
    public void setUltimoRecalculo(LocalDateTime ultimoRecalculo) { this.ultimoRecalculo = ultimoRecalculo; }

    public LocalDateTime getCanceladoEm() { return canceladoEm; }
    public void setCanceladoEm(LocalDateTime canceladoEm) { this.canceladoEm = canceladoEm; }

    public String getMotivoCancelamento() { return motivoCancelamento; }
    public void setMotivoCancelamento(String motivoCancelamento) { this.motivoCancelamento = motivoCancelamento; }

    public List<EventoPlanoEntity> getEventos() { return eventos; }
    public void setEventos(List<EventoPlanoEntity> eventos) { this.eventos = eventos; }
}
