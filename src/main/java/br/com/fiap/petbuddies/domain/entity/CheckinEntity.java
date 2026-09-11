package br.com.fiap.petbuddies.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Guarda a transcricao, nao o audio — nao ha coluna de audio.
// Sem coluna de status: a linha so nasce quando o tutor confirma.
@Entity
@Table(name = "T_PB_CHECKIN")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CheckinEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CHECKIN")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_ANIMAL", nullable = false)
    private AnimalEntity animal;

    /** Nulo = relato geral do dia. Preenchido = relato sobre este item. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_ITEM_PLANO_CUIDADO")
    private ItemPlanoCuidadoEntity itemPlanoCuidado;

    @Column(name = "DT_REFERENCIA", nullable = false)
    private LocalDate dataReferencia;

    @Column(name = "DH_REGISTRADO_EM", nullable = false)
    private LocalDateTime registradoEm;

    // CLOB: sem @Lob o Hibernate declara VARCHAR2(255) e o validate recusa a subida.
    @Lob
    @Column(name = "TX_NARRATIVA", nullable = false)
    private String narrativa;

    @Lob
    @Column(name = "TX_OBSERVACOES_GERAIS")
    private String observacoesGerais;

    @Column(name = "DS_TIC_UTILIZADA", length = 120)
    private String ticUtilizada;

    @Column(name = "CA_CREATED_AT", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private LocalDateTime createdAt;

    @Column(name = "AT_UPDATED_AT")
    @Setter(AccessLevel.NONE)
    private LocalDateTime updatedAt;

    @PrePersist
    private void prePersist() { createdAt = LocalDateTime.now(); }

    @PreUpdate
    private void preUpdate() { updatedAt = LocalDateTime.now(); }
}
