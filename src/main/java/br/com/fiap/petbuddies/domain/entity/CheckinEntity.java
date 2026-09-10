package br.com.fiap.petbuddies.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * O relato do tutor: um por animal e por dia (decisão D), ou um por item
 * quando o relato é sobre um cuidado específico (ADR s3-24 §6).
 *
 * <p>Guarda a TRANSCRIÇÃO, não o áudio (ADR s3-18) — não há coluna de áudio.
 * Sem coluna de status: a linha só nasce quando o tutor confirma o que a IA
 * entendeu (§5.1 do documento de IA, passo E).</p>
 */
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
