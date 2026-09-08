package br.com.fiap.petbuddies.domain.entity;

import br.com.fiap.petbuddies.domain.enums.StatusEventoPlano;
import br.com.fiap.petbuddies.domain.enums.TipoEventoProtocolo;
import br.com.fiap.petbuddies.domain.enums.TipoOrigemItem;
import jakarta.persistence.*;
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
@Table(name = "T_PB_EVENTO_PLANO")
public class EventoPlanoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_EVENTO_PLANO")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PLANO_CUIDADO_ANIMAL", nullable = false)
    private PlanoCuidadoAnimalEntity plano;

    /** Preenchido quando a origem e PROTOCOLO: o molde que gerou este item. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_EVENTO_PROTOCOLO")
    private EventoProtocoloEntity eventoProtocolo;

    @Enumerated(EnumType.STRING)
    @Column(name = "TP_ORIGEM", nullable = false, length = 20)
    private TipoOrigemItem origem;

    /** Preenchido quando a origem e PRESCRICAO. Id da prescricao no servico .NET. */
    @Column(name = "ID_PRESCRICAO")
    private Long prescricaoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "TP_TIPO", nullable = false, length = 50)
    private TipoEventoProtocolo tipo;

    @Column(name = "NM_NOME", nullable = false)
    private String nome;

    @Column(name = "DT_DATA_ALVO", nullable = false)
    private LocalDate dataAlvo;

    @Enumerated(EnumType.STRING)
    @Column(name = "ST_STATUS", nullable = false, length = 50)
    private StatusEventoPlano status = StatusEventoPlano.PENDENTE;

    @Column(name = "OB_OBSERVACAO", length = 2000)
    private String observacao;

    /** Id do procedimento no servico .NET, quando o item foi reconciliado. */
    @Column(name = "ID_PROCEDIMENTO")
    private Long procedimentoId;

    @Column(name = "DT_EXECUTADO_EM")
    private LocalDateTime executadoEm;

    @Column(name = "AT_UPDATED_AT")
    private LocalDateTime updatedAt;

    @PreUpdate
    private void preUpdate() { updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PlanoCuidadoAnimalEntity getPlano() { return plano; }
    public void setPlano(PlanoCuidadoAnimalEntity plano) { this.plano = plano; }

    public EventoProtocoloEntity getEventoProtocolo() { return eventoProtocolo; }
    public void setEventoProtocolo(EventoProtocoloEntity ep) { this.eventoProtocolo = ep; }

    public TipoOrigemItem getOrigem() { return origem; }
    public void setOrigem(TipoOrigemItem origem) { this.origem = origem; }

    public Long getPrescricaoId() { return prescricaoId; }
    public void setPrescricaoId(Long prescricaoId) { this.prescricaoId = prescricaoId; }

    public TipoEventoProtocolo getTipo() { return tipo; }
    public void setTipo(TipoEventoProtocolo tipo) { this.tipo = tipo; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public LocalDate getDataAlvo() { return dataAlvo; }
    public void setDataAlvo(LocalDate dataAlvo) { this.dataAlvo = dataAlvo; }

    public StatusEventoPlano getStatus() { return status; }
    public void setStatus(StatusEventoPlano status) { this.status = status; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }

    public Long getProcedimentoId() { return procedimentoId; }
    public void setProcedimentoId(Long procedimentoId) { this.procedimentoId = procedimentoId; }

    public LocalDateTime getExecutadoEm() { return executadoEm; }
    public void setExecutadoEm(LocalDateTime executadoEm) { this.executadoEm = executadoEm; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
