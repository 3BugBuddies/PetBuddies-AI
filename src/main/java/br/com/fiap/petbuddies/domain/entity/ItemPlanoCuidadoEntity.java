package br.com.fiap.petbuddies.domain.entity;

import br.com.fiap.petbuddies.domain.embeddable.Desfecho;
import br.com.fiap.petbuddies.domain.enums.cuidado.StatusItem;
import br.com.fiap.petbuddies.domain.enums.cuidado.TipoCuidado;
import br.com.fiap.petbuddies.domain.enums.cuidado.TipoOrigemItem;
import jakarta.persistence.*;
import lombok.*;
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

    // Preenchido quando origem e PROTOCOLO; referencia solta, sem relacao JPA.
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

    // Nulo enquanto o item nao passa por check-in: item de origem PROTOCOLO nao tem dose/checkin.
    @Embedded
    private Desfecho desfecho;

    @Column(name = "AT_UPDATED_AT")
    @Setter(AccessLevel.NONE)
    private LocalDateTime updatedAt;

    @PreUpdate
    private void preUpdate() { updatedAt = LocalDateTime.now(); }
}
