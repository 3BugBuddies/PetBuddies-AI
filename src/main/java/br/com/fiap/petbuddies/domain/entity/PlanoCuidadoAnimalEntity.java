package br.com.fiap.petbuddies.domain.entity;

import br.com.fiap.petbuddies.domain.enums.StatusPlano;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "T_PB_PLANO_CUIDADO_ANIMAL")
public class PlanoCuidadoAnimalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_pb_plano")
    @SequenceGenerator(name = "seq_pb_plano", sequenceName = "SEQ_T_PB_PLANO", allocationSize = 1)
    @Column(name = "ID_PLANO_CUIDADO_ANIMAL")
    private Long id;

    @Column(name = "ID_PET_NET_ANIMAL", nullable = false)
    private Long petNetApiAnimalId;

    @Column(name = "ID_PET_NET_CONSULTA")
    private Long petNetApiConsultaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PROTOCOLO", nullable = false)
    private ProtocoloEntity protocolo;

    @Enumerated(EnumType.STRING)
    @Column(name = "ST_STATUS", nullable = false)
    private StatusPlano status = StatusPlano.ATIVO;

    @Column(name = "CA_CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "AT_UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "NR_SCORE_ATUAL")
    private Integer scoreAtual;

    @Column(name = "DT_ULTIMO_RECALCULO")
    private LocalDateTime ultimoRecalculo;

    @Column(name = "DT_CANCELADO_EM")
    private LocalDateTime canceladoEm;

    @Column(name = "MT_MOTIVO_CANCELAMENTO")
    private String motivoCancelamento;

    @OneToMany(mappedBy = "plano", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EventoPlanoEntity> eventos = new ArrayList<>();

    @PrePersist
    private void prePersist() { createdAt = LocalDateTime.now(); }

    @PreUpdate
    private void preUpdate() { updatedAt = LocalDateTime.now(); }

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

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }

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
