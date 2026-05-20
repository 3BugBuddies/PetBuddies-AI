package br.com.fiap.petbuddies.domain.entity;

import br.com.fiap.petbuddies.domain.enums.CanalNotificacao;
import br.com.fiap.petbuddies.domain.enums.StatusNotificacao;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "T_PB_NOTIFICACAO")
public class NotificacaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_pb_notificacao")
    @SequenceGenerator(name = "seq_pb_notificacao", sequenceName = "SEQ_T_PB_NOTIFICACAO", allocationSize = 1)
    @Column(name = "ID_NOTIFICACAO")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_EVENTO_PLANO", nullable = false)
    private EventoPlanoEntity eventoPlano;

    @Column(name = "ID_PET_NET_ANIMAL", nullable = false)
    private Long petNetApiAnimalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "TP_CANAL", nullable = false)
    private CanalNotificacao canal = CanalNotificacao.WHATSAPP;

    @Enumerated(EnumType.STRING)
    @Column(name = "ST_STATUS_ENVIO", nullable = false)
    private StatusNotificacao statusEnvio = StatusNotificacao.PENDENTE;

    @Column(name = "TX_PAYLOAD", length = 1000)
    private String payload;

    @Column(name = "NR_TENTATIVAS", nullable = false)
    private Integer tentativas = 0;

    @Column(name = "DT_ULTIMA_TENTATIVA")
    private LocalDateTime ultimaTentativa;

    @Column(name = "CA_CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void prePersist() { createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public EventoPlanoEntity getEventoPlano() { return eventoPlano; }
    public void setEventoPlano(EventoPlanoEntity eventoPlano) { this.eventoPlano = eventoPlano; }
    public Long getPetNetApiAnimalId() { return petNetApiAnimalId; }
    public void setPetNetApiAnimalId(Long petNetApiAnimalId) { this.petNetApiAnimalId = petNetApiAnimalId; }
    public CanalNotificacao getCanal() { return canal; }
    public void setCanal(CanalNotificacao canal) { this.canal = canal; }
    public StatusNotificacao getStatusEnvio() { return statusEnvio; }
    public void setStatusEnvio(StatusNotificacao statusEnvio) { this.statusEnvio = statusEnvio; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public Integer getTentativas() { return tentativas; }
    public void setTentativas(Integer tentativas) { this.tentativas = tentativas; }
    public LocalDateTime getUltimaTentativa() { return ultimaTentativa; }
    public void setUltimaTentativa(LocalDateTime ultimaTentativa) { this.ultimaTentativa = ultimaTentativa; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
