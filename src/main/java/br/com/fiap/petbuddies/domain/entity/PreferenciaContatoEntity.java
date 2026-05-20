package br.com.fiap.petbuddies.domain.entity;

import br.com.fiap.petbuddies.domain.enums.CanalNotificacao;
import br.com.fiap.petbuddies.domain.enums.JanelaPreferida;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "T_PB_PREFERENCIA_CONTATO")
public class PreferenciaContatoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_pb_preferencia_contato")
    @SequenceGenerator(name = "seq_pb_preferencia_contato", sequenceName = "SEQ_T_PB_PREFERENCIA_CONTATO", allocationSize = 1)
    @Column(name = "ID_PREFERENCIA_CONTATO")
    private Long id;

    @Column(name = "ID_PET_NET_RESPONSAVEL", nullable = false, unique = true)
    private Long petNetApiResponsavelId;

    @Enumerated(EnumType.STRING)
    @Column(name = "TP_CANAL", nullable = false)
    private CanalNotificacao canal = CanalNotificacao.WHATSAPP;

    @Column(name = "BL_OPT_IN", nullable = false)
    private Boolean optInNotificacoes = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "TP_JANELA", nullable = false)
    private JanelaPreferida janelaPreferida = JanelaPreferida.TARDE;

    @Column(name = "CA_CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "AT_UPDATED_AT")
    private LocalDateTime updatedAt;

    @PrePersist
    private void prePersist() { createdAt = LocalDateTime.now(); }

    @PreUpdate
    private void preUpdate() { updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPetNetApiResponsavelId() { return petNetApiResponsavelId; }
    public void setPetNetApiResponsavelId(Long petNetApiResponsavelId) { this.petNetApiResponsavelId = petNetApiResponsavelId; }
    public CanalNotificacao getCanal() { return canal; }
    public void setCanal(CanalNotificacao canal) { this.canal = canal; }
    public Boolean getOptInNotificacoes() { return optInNotificacoes; }
    public void setOptInNotificacoes(Boolean optInNotificacoes) { this.optInNotificacoes = optInNotificacoes; }
    public JanelaPreferida getJanelaPreferida() { return janelaPreferida; }
    public void setJanelaPreferida(JanelaPreferida janelaPreferida) { this.janelaPreferida = janelaPreferida; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
