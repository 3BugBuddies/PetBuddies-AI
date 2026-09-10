package br.com.fiap.petbuddies.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Ato assinado pelo veterinário no fechamento do atendimento. Imutável por
 * contrato (ADR s3-09): corrigir significa emitir outra prescrição, não editar
 * esta — {@code @Immutable} é a garantia do ORM, e por isso não há PUT nem
 * DELETE no controller. AT_UPDATED_AT fica sempre nulo.
 */
@Entity
@Immutable
@Table(name = "T_PB_PRESCRICAO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrescricaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PRESCRICAO")
    private Long id;

    @Column(name = "NM_MEDICAMENTO", nullable = false, length = 150)
    private String medicamento;

    @Column(name = "NR_DOSE_MIN", nullable = false, precision = 8, scale = 3)
    private BigDecimal doseMin;

    @Column(name = "NR_DOSE_MAX", nullable = false, precision = 8, scale = 3)
    private BigDecimal doseMax;

    @Column(name = "DS_UNIDADE", nullable = false, length = 20)
    private String unidade;

    @Column(name = "NR_FREQUENCIA_DIA", nullable = false)
    private Integer frequenciaDia;

    @Column(name = "NR_DURACAO_DIAS", nullable = false)
    private Integer duracaoDias;

    @Column(name = "DT_INICIO", nullable = false)
    private LocalDate dataInicio;

    // CLOB: sem @Lob o Hibernate declara VARCHAR2(255) e o validate recusa a subida.
    @Lob
    @Column(name = "TX_ORIENTACAO")
    private String orientacao;

    /** Aponta para o bulário, que só existe na Sprint 4. Nasce nulo, sem FK. */
    @Column(name = "ID_MATERIAL_ORIGEM")
    private Long materialOrigemId;

    @Column(name = "NR_VERSAO_ORIGEM")
    private Integer versaoOrigem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_ANIMAL", nullable = false)
    private AnimalEntity animal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_VETERINARIO", nullable = false)
    private VeterinarioEntity veterinario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_REGISTRO_ATENDIMENTO", nullable = false)
    private RegistroAtendimentoEntity registroAtendimento;

    @Column(name = "CA_CREATED_AT", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private LocalDateTime createdAt;

    @Column(name = "AT_UPDATED_AT")
    @Setter(AccessLevel.NONE)
    private LocalDateTime updatedAt;

    @PrePersist
    private void prePersist() { createdAt = LocalDateTime.now(); }
}
