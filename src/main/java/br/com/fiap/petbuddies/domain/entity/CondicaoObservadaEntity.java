package br.com.fiap.petbuddies.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;
import org.hibernate.type.NumericBooleanConverter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Uma condição extraída da narrativa pela IA, confirmada pelo tutor: uma
 * linha por condição observada. Aponta para o CATÁLOGO, não para a regra — o
 * tutor pode relatar condição que nenhuma regra de prescrição cobre.
 *
 * <p>Imutável (AT_UPDATED_AT nasce e fica nulo): é o que foi confirmado
 * naquele check-in, não se corrige — um relato incorreto gera um novo
 * check-in.</p>
 *
 * <p>{@code CD_CODIGO_CONGELADO} guarda o código do catálogo no momento da
 * observação, no mesmo espírito do rótulo congelado em
 * {@link RegraPrescricaoEntity} (ADR s3-10).</p>
 */
@Entity
@Immutable
@Table(name = "T_PB_CONDICAO_OBSERVADA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CondicaoObservadaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CONDICAO_OBSERVADA")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_CHECKIN", nullable = false)
    private CheckinEntity checkin;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_CONDICAO_CLINICA", nullable = false)
    private CondicaoClinicaEntity condicaoClinica;

    @Column(name = "CD_CODIGO_CONGELADO", nullable = false, length = 60)
    private String codigoCongelado;

    // Nulo quando a condicao e NUMERICO. JPA nao invoca o converter para valor
    // nulo, entao o Boolean (wrapper) fica nulo em vez de virar 0 (CK_COBS_UM_VALOR).
    @Convert(converter = NumericBooleanConverter.class)
    @Column(name = "BL_VALOR_BOOLEANO")
    private Boolean valorBooleano;

    // Nulo quando a condicao e BOOLEANO (CK_COBS_UM_VALOR).
    @Column(name = "NR_VALOR_NUMERICO", precision = 10, scale = 3)
    private BigDecimal valorNumerico;

    // Confianca da extracao por campo, nao uma global (contrato §9.1). CK_COBS_CONFIANCA: entre 0 e 1.
    @Column(name = "NR_CONFIANCA", nullable = false, precision = 5, scale = 4)
    private BigDecimal confianca;

    @Column(name = "CA_CREATED_AT", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private LocalDateTime createdAt;

    @Column(name = "AT_UPDATED_AT")
    @Setter(AccessLevel.NONE)
    private LocalDateTime updatedAt;

    @PrePersist
    private void prePersist() { createdAt = LocalDateTime.now(); }
}
