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

// Imutavel — regra assinada nao se corrige, substitui-se numa nova prescricao.
// Rotulo, tipo e fonte sao copiados no momento da criacao e nunca mudam — check-in avalia sem consultar o catalogo.
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
