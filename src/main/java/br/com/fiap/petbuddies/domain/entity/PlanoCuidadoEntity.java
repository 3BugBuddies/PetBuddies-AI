package br.com.fiap.petbuddies.domain.entity;

import br.com.fiap.petbuddies.domain.enums.cuidado.CategoriaPlano;
import br.com.fiap.petbuddies.domain.enums.cuidado.StatusPlano;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "T_PB_PLANO_CUIDADO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlanoCuidadoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PLANO_CUIDADO")
    private Long id;

    @Column(name = "ID_ANIMAL", nullable = false)
    private Long animalId;

    @Column(name = "ID_CONSULTA")
    private Long consultaId;

    // Nulo quando o plano nao nasce de protocolo (ex.: so prescricao).
    @Column(name = "ID_PROTOCOLO")
    private Long protocoloId;

    // Propria do plano, nao herdada via join com protocolo.
    @Enumerated(EnumType.STRING)
    @Column(name = "TP_CATEGORIA_PLANO", nullable = false, length = 20)
    private CategoriaPlano categoria;

    @Enumerated(EnumType.STRING)
    @Column(name = "ST_STATUS_PLANO", nullable = false, length = 50)
    private StatusPlano status = StatusPlano.ATIVO;

    @Column(name = "CA_CREATED_AT", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private LocalDateTime createdAt;

    @Column(name = "AT_UPDATED_AT")
    @Setter(AccessLevel.NONE)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "plano", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ItemPlanoCuidadoEntity> itens = new ArrayList<>();

    @PrePersist
    private void prePersist() { createdAt = LocalDateTime.now(); }

    @PreUpdate
    private void preUpdate() { updatedAt = LocalDateTime.now(); }
}
