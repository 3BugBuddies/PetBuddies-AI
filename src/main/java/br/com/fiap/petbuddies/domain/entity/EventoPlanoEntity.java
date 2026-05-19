package br.com.fiap.petbuddies.domain.entity;

import br.com.fiap.petbuddies.domain.enums.StatusEventoPlano;
import br.com.fiap.petbuddies.domain.enums.TipoEventoProtocolo;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "evento_plano")
public class EventoPlanoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plano_id", nullable = false)
    private PlanoCuidadoAnimalEntity plano;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_protocolo_id")
    private EventoProtocoloEntity eventoProtocolo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEventoProtocolo tipo;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private LocalDate dataAlvo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusEventoPlano status = StatusEventoPlano.PENDENTE;

    private String observacao;

    @Column
    private Long petNetApiProcedimentoId;

    @Column
    private LocalDateTime executadoEm;

    @Column(nullable = false)
    private int tentativas = 0;

    private LocalDateTime atualizadoEm;

    @PreUpdate
    private void preUpdate() {
        atualizadoEm = LocalDateTime.now();
    }

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
    public void setEventoProtocolo(EventoProtocoloEntity eventoProtocolo) { this.eventoProtocolo = eventoProtocolo; }

    public Long getPetNetApiProcedimentoId() { return petNetApiProcedimentoId; }
    public void setPetNetApiProcedimentoId(Long petNetApiProcedimentoId) { this.petNetApiProcedimentoId = petNetApiProcedimentoId; }

    public LocalDateTime getExecutadoEm() { return executadoEm; }
    public void setExecutadoEm(LocalDateTime executadoEm) { this.executadoEm = executadoEm; }

    public int getTentativas() { return tentativas; }
    public void setTentativas(int tentativas) { this.tentativas = tentativas; }

    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
}
