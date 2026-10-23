package br.com.fiap.petbuddies.domain.entity;

import br.com.fiap.petbuddies.domain.embeddable.Auditoria;
import br.com.fiap.petbuddies.domain.enums.prescricao.OperadorRegra;
import br.com.fiap.petbuddies.domain.enums.prescricao.TipoAcaoRegra;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "T_PB_REGRA_RECEITA_MODELO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegraReceitaModeloEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_REGRA_RECEITA_MODELO")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_RECEITA_MODELO", nullable = false)
    private ReceitaModeloEntity receitaModelo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_CONDICAO_CLINICA", nullable = false)
    private CondicaoClinicaEntity condicaoClinica;

    // Nulos juntos (CK_REGRA_RM_LIMITE); a regra por tipo da condição é do service.
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

    @PreUpdate
    private void preUpdate() { auditoria = auditoria.atualizadaAgora(); }
}
