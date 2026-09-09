package br.com.fiap.petbuddies.domain.entity;

import br.com.fiap.petbuddies.domain.enums.StatusConsulta;
import br.com.fiap.petbuddies.domain.enums.TipoConsulta;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "T_PB_CONSULTA")
public class ConsultaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CONSULTA")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "TP_TIPO_CONSULTA", nullable = false, length = 50)
    private TipoConsulta tipo;

    @Column(name = "DH_DATA_HORA", nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(name = "ST_STATUS_CONSULTA", nullable = false, length = 50)
    private StatusConsulta status;

    @Column(name = "OB_OBSERVACAO", length = 2000)
    private String observacao;

    @Column(name = "MT_MOTIVO", length = 2000)
    private String motivo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_ANIMAL", nullable = false)
    private AnimalEntity animal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_VETERINARIO", nullable = false)
    private VeterinarioEntity veterinario;

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

    public TipoConsulta getTipo() { return tipo; }
    public void setTipo(TipoConsulta tipo) { this.tipo = tipo; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public StatusConsulta getStatus() { return status; }
    public void setStatus(StatusConsulta status) { this.status = status; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public AnimalEntity getAnimal() { return animal; }
    public void setAnimal(AnimalEntity animal) { this.animal = animal; }

    public VeterinarioEntity getVeterinario() { return veterinario; }
    public void setVeterinario(VeterinarioEntity veterinario) { this.veterinario = veterinario; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
