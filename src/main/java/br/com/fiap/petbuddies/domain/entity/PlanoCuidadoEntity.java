package br.com.fiap.petbuddies.domain.entity;

import br.com.fiap.petbuddies.domain.enums.CategoriaPlano;
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
 * tabelas do servico .NET e usam o mesmo nome que la e a chave primaria.
 * ID_PROTOCOLO segue a mesma forma: o catalogo passou ao .NET no ADR s3-25, e a
 * FK virou referencia solta.</p>
 *
 * <p>A CATEGORIA E PROPRIA DO PLANO, e nao herdada do protocolo (ADR s3-24 §4b).
 * Ao aplicar um protocolo ela e copiada dele; um plano de tratamento tem
 * protocolo nulo e categoria propria. Antes disso a categoria so existia via
 * join com protocolo, e a consulta de idempotencia excluia todo plano sem
 * molde — que e justamente o de tratamento.</p>
 */
@Entity
@Table(name = "T_PB_PLANO_CUIDADO")
public class PlanoCuidadoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PLANO_CUIDADO")
    private Long id;

    @Column(name = "ID_ANIMAL", nullable = false)
    private Long animalId;

    @Column(name = "ID_CONSULTA")
    private Long consultaId;

    @Column(name = "ID_PROTOCOLO")
    private Long protocoloId;

    @Enumerated(EnumType.STRING)
    @Column(name = "TP_CATEGORIA_PLANO", nullable = false, length = 20)
    private CategoriaPlano categoria;

    @Enumerated(EnumType.STRING)
    @Column(name = "ST_STATUS_PLANO", nullable = false, length = 50)
    private StatusPlano status = StatusPlano.ATIVO;

    @Column(name = "CA_CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "AT_UPDATED_AT")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "plano", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ItemPlanoCuidadoEntity> itens = new ArrayList<>();

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

    public Long getProtocoloId() { return protocoloId; }
    public void setProtocoloId(Long protocoloId) { this.protocoloId = protocoloId; }

    public CategoriaPlano getCategoria() { return categoria; }
    public void setCategoria(CategoriaPlano categoria) { this.categoria = categoria; }

    public StatusPlano getStatus() { return status; }
    public void setStatus(StatusPlano status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public List<ItemPlanoCuidadoEntity> getItens() { return itens; }
    public void setItens(List<ItemPlanoCuidadoEntity> itens) { this.itens = itens; }
}
