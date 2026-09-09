package br.com.fiap.petbuddies.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "T_PB_JANELA_ATENDIMENTO",
    uniqueConstraints = @UniqueConstraint(
        name = "UK_JANELA_VET_INICIO",
        columnNames = {"ID_VETERINARIO", "DH_DATA_HORA_INICIO"})
)
public class JanelaAtendimentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_JANELA_ATENDIMENTO")
    private Long id;

    @Column(name = "DH_DATA_HORA_INICIO", nullable = false)
    private LocalDateTime dataHoraInicio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_VETERINARIO", nullable = false)
    private VeterinarioEntity veterinario;

    // Nula = horário livre. FK_JANELA_CONSULTA é ON DELETE SET NULL; cancelamento
    // de consulta não é exclusão e precisa apagar o vínculo à mão (dívida conhecida).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CONSULTA")
    private ConsultaEntity consulta;

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

    public LocalDateTime getDataHoraInicio() { return dataHoraInicio; }
    public void setDataHoraInicio(LocalDateTime dataHoraInicio) { this.dataHoraInicio = dataHoraInicio; }

    public VeterinarioEntity getVeterinario() { return veterinario; }
    public void setVeterinario(VeterinarioEntity veterinario) { this.veterinario = veterinario; }

    public ConsultaEntity getConsulta() { return consulta; }
    public void setConsulta(ConsultaEntity consulta) { this.consulta = consulta; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
