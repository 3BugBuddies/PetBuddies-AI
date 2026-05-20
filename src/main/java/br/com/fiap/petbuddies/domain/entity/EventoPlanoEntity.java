package br.com.fiap.petbuddies.domain.entity;

import br.com.fiap.petbuddies.domain.enums.StatusEventoPlano;
import br.com.fiap.petbuddies.domain.enums.TipoEventoProtocolo;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "T_PB_EVENTO_PLANO")
public class EventoPlanoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_pb_evento_plano")
    @SequenceGenerator(name = "seq_pb_evento_plano", sequenceName = "SEQ_T_PB_EVENTO_PLANO", allocationSize = 1)
    @Column(name = "ID_EVENTO_PLANO")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PLANO_CUIDADO_ANIMAL", nullable = false)
    private PlanoCuidadoAnimalEntity plano;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_EVENTO_PROTOCOLO")
    private EventoProtocoloEntity eventoProtocolo;

    @Enumerated(EnumType.STRING)
    @Column(name = "TP_TIPO", nullable = false)
    private TipoEventoProtocolo tipo;

    @Column(name = "NM_NOME", nullable = false)
    private String nome;

    @Column(name = "DT_DATA_ALVO", nullable = false)
    private LocalDate dataAlvo;

    @Enumerated(EnumType.STRING)
    @Column(name = "ST_STATUS", nullable = false)
    private StatusEventoPlano status = StatusEventoPlano.PENDENTE;

    @Column(name = "OB_OBSERVACAO")
    private String observacao;

    @Column(name = "ID_PET_NET_PROCEDIMENTO")
    private Long petNetApiProcedimentoId;

    @Column(name = "DT_EXECUTADO_EM")
    private LocalDateTime executadoEm;

    @Column(name = "NR_TENTATIVAS", nullable = false)
    private int tentativas = 0;

    @Column(name = "AT_UPDATED_AT")
    private LocalDateTime updatedAt;

    @PreUpdate
    private void preUpdate() { updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PlanoCuidadoAnimalEntity getPlano() { return plano; }
    public void setPlano(PlanoCuidadoAnimalEntity plano) { this.plano = plano; }

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

    public EventoProtocoloEntity getEventoProtocolo() { return eventoProtocolo; }
    public void setEventoProtocolo(EventoProtocoloEntity ep) { this.eventoProtocolo = ep; }

    public Long getPetNetApiProcedimentoId() { return petNetApiProcedimentoId; }
    public void setPetNetApiProcedimentoId(Long id) { this.petNetApiProcedimentoId = id; }

    public LocalDateTime getExecutadoEm() { return executadoEm; }
    public void setExecutadoEm(LocalDateTime executadoEm) { this.executadoEm = executadoEm; }

    public int getTentativas() { return tentativas; }
    public void setTentativas(int tentativas) { this.tentativas = tentativas; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
