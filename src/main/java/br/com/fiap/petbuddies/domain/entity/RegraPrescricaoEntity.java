package br.com.fiap.petbuddies.domain.entity;

import br.com.fiap.petbuddies.domain.enums.prescricao.OperadorRegra;
import br.com.fiap.petbuddies.domain.enums.prescricao.TipoAcaoRegra;
import br.com.fiap.petbuddies.domain.enums.prescricao.TipoDado;
import br.com.fiap.petbuddies.domain.enums.prescricao.TipoFonteValor;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Condição → ação sobre a dose de uma prescrição. Imutável como a prescrição
 * que a carrega — regra assinada não se corrige, se substitui numa nova
 * prescrição.
 *
 * <p>O rótulo, o tipo de dado e a fonte são copiados da {@link CondicaoClinicaEntity}
 * no momento da criação e nunca mais mudam (ADR s3-10), mesmo que o catálogo
 * mude depois — é o que permite o check-in avaliar a regra sem consultar o
 * catálogo.</p>
 */
@Entity
@Immutable
@Table(name = "T_PB_REGRA_PRESCRICAO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegraPrescricaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_REGRA_PRESCRICAO")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_PRESCRICAO", nullable = false)
    private PrescricaoEntity prescricao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_CONDICAO_CLINICA", nullable = false)
    private CondicaoClinicaEntity condicaoClinica;

    @Column(name = "DS_ROTULO_CONGELADO", nullable = false, length = 255)
    private String rotuloCongelado;

    @Enumerated(EnumType.STRING)
    @Column(name = "TP_DADO_CONGELADO", nullable = false, length = 20)
    private TipoDado tipoDadoCongelado;

    @Enumerated(EnumType.STRING)
    @Column(name = "TP_FONTE_VALOR_CONGELADA", nullable = false, length = 20)
    private TipoFonteValor fonteValorCongelada;

    // Nulo quando tipoDadoCongelado é BOOLEANO — CK_REGRA_COERENCIA.
    @Enumerated(EnumType.STRING)
    @Column(name = "TP_OPERADOR", length = 20)
    private OperadorRegra operador;

    @Column(name = "NR_LIMITE", precision = 10, scale = 3)
    private BigDecimal limite;

    @Enumerated(EnumType.STRING)
    @Column(name = "TP_ACAO_DOSE", nullable = false, length = 30)
    private TipoAcaoRegra acaoDose;

    @Column(name = "NR_ORDEM", nullable = false)
    private Integer ordem;

    @Column(name = "CA_CREATED_AT", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private LocalDateTime createdAt;

    @Column(name = "AT_UPDATED_AT")
    @Setter(AccessLevel.NONE)
    private LocalDateTime updatedAt;

    @PrePersist
    private void prePersist() { createdAt = LocalDateTime.now(); }
}
