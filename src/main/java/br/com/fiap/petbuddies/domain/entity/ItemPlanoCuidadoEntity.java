package br.com.fiap.petbuddies.domain.entity;

import br.com.fiap.petbuddies.domain.enums.cuidado.StatusItem;
import br.com.fiap.petbuddies.domain.enums.checkin.TipoDesfecho;
import br.com.fiap.petbuddies.domain.enums.cuidado.TipoCuidado;
import br.com.fiap.petbuddies.domain.enums.cuidado.TipoOrigemItem;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Um item concreto do plano de um animal, com data alvo e status.
 *
 * <p>A origem diz de onde o item veio e nao tem default: PROTOCOLO vem do molde
 * do catalogo, PRESCRICAO vem de um ato assinado pelo veterinario. O rotulo
 * "na clinica" / "voce faz" e derivado dela, e por isso nao existe coluna de
 * executor.</p>
 */
@Entity
@Table(name = "T_PB_ITEM_PLANO_CUIDADO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemPlanoCuidadoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ITEM_PLANO_CUIDADO")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PLANO_CUIDADO", nullable = false)
    private PlanoCuidadoEntity plano;

    /**
     * Preenchido quando a origem e PROTOCOLO: o molde que gerou este item.
     * Referencia solta desde o ADR s3-25 — o catalogo passou ao .NET.
     */
    @Column(name = "ID_REGRA_PROTOCOLO")
    private Long regraProtocoloId;

    @Enumerated(EnumType.STRING)
    @Column(name = "TP_ORIGEM", nullable = false, length = 20)
    private TipoOrigemItem origem;

    /** Preenchido quando a origem e PRESCRICAO. Id da prescricao no servico .NET. */
    @Column(name = "ID_PRESCRICAO")
    private Long prescricaoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "TP_TIPO_CUIDADO", nullable = false, length = 50)
    private TipoCuidado tipo;

    @Column(name = "NM_NOME", nullable = false)
    private String nome;

    @Column(name = "DT_DATA_ALVO", nullable = false)
    private LocalDate dataAlvo;

    @Enumerated(EnumType.STRING)
    @Column(name = "ST_STATUS_ITEM", nullable = false, length = 50)
    private StatusItem status = StatusItem.PENDENTE;

    @Column(name = "OB_OBSERVACAO", length = 2000)
    private String observacao;

    /** Id do procedimento no servico .NET, quando o item foi reconciliado. */
    @Column(name = "ID_PROCEDIMENTO")
    private Long procedimentoId;

    @Column(name = "DT_EXECUTADO_EM")
    private LocalDateTime executadoEm;

    // --- A OUTRA METADE DO PADRAO DE BAIXA (ADR s3-24 §5) --------------------
    // O item da clinica e baixado por um procedimento (ID_PROCEDIMENTO,
    // DT_EXECUTADO_EM, acima). O item de casa e baixado por um check-in, e sao
    // estas quatro colunas: elas eram T_PB_CHECKIN_RESULTADO, que deixou de
    // existir. Nascem nulaveis porque item de origem PROTOCOLO nao tem dose, e
    // NINGUEM AS ESCREVE NESTE PR — quem escreve e o PR-J6.

    @Column(name = "ID_CHECKIN")
    private Long checkinId;

    @Enumerated(EnumType.STRING)
    @Column(name = "TP_DESFECHO", length = 30)
    private TipoDesfecho desfecho;

    @Column(name = "NR_DOSE_APLICADA", precision = 8, scale = 3)
    private BigDecimal doseAplicada;

    @Column(name = "ID_REGRA_APLICADA")
    private Long regraAplicadaId;

    @Column(name = "AT_UPDATED_AT")
    @Setter(AccessLevel.NONE)
    private LocalDateTime updatedAt;

    @PreUpdate
    private void preUpdate() { updatedAt = LocalDateTime.now(); }
}
