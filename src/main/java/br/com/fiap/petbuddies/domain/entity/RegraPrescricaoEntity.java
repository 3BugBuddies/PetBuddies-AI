package br.com.fiap.petbuddies.domain.entity;

import br.com.fiap.petbuddies.domain.embeddable.Auditoria;
import br.com.fiap.petbuddies.domain.embeddable.CondicaoCongelada;
import br.com.fiap.petbuddies.domain.enums.prescricao.OperadorRegra;
import br.com.fiap.petbuddies.domain.enums.prescricao.TipoAcaoRegra;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

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

    @Embedded
    private CondicaoCongelada condicaoCongelada;

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

    @Embedded
    @Setter(AccessLevel.NONE)
    private Auditoria auditoria;

    @PrePersist
    private void prePersist() { auditoria = Auditoria.criadaAgora(); }
}
