package br.com.fiap.petbuddies.domain.entity;

import br.com.fiap.petbuddies.domain.enums.StatusPlano;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * O plano de cuidado vivo de um animal.
 *
 * <p>O protocolo e opcional: um plano formado apenas por itens de prescricao nao
 * nasce de nenhum molde do catalogo. Os ids de animal e consulta apontam para
 * tabelas do servico .NET e usam o mesmo nome que la e a chave primaria.</p>
 */
@Entity
@Table(name = "T_PB_PLANO_CUIDADO_ANIMAL")
public class PlanoCuidadoAnimalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PLANO_CUIDADO_ANIMAL")
    private Long id;

    @Column(name = "ID_ANIMAL", nullable = false)
    private Long animalId;

    @Column(name = "ID_CONSULTA")
    private Long consultaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PROTOCOLO")
    private ProtocoloEntity protocolo;

    @Enumerated(EnumType.STRING)
    @Column(name = "ST_STATUS", nullable = false, length = 50)
    private StatusPlano status = StatusPlano.ATIVO;

    @Column(name = "CA_CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "AT_UPDATED_AT")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "plano", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EventoPlanoEntity> eventos = new ArrayList<>();

    @PrePersist
    private void prePersist() { createdAt = LocalDateTime.now(); }

    @PreUpdate
    private void preUpdate() { updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAnimalId() { return animalId; }
    public void setAnimalId(Long animalId) { this.animalId = animalId; }

    public Long getConsultaId() { return consultaId; }
    public void setConsultaId(Long consultaId) { this.consultaId = consultaId; }

    public ProtocoloEntity getProtocolo() { return protocolo; }
    public void setProtocolo(ProtocoloEntity protocolo) { this.protocolo = protocolo; }

    public StatusPlano getStatus() { return status; }
    public void setStatus(StatusPlano status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public List<EventoPlanoEntity> getEventos() { return eventos; }
    public void setEventos(List<EventoPlanoEntity> eventos) { this.eventos = eventos; }
}
