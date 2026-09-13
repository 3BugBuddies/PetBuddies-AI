package br.com.fiap.petbuddies.domain.entity;

import br.com.fiap.petbuddies.domain.embeddable.ValorObservado;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Aponta para o catalogo, nao para a regra — tutor pode relatar condicao que nenhuma regra cobre.
// Imutavel: relato incorreto gera um novo check-in, nao corrige este.
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

    @Embedded
    private ValorObservado valor;

    // Confianca por campo, nao global. CK_COBS_CONFIANCA: entre 0 e 1.
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
