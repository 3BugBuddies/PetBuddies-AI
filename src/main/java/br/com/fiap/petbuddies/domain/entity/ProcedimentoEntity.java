package br.com.fiap.petbuddies.domain.entity;

import br.com.fiap.petbuddies.domain.enums.StatusProcedimento;
import br.com.fiap.petbuddies.domain.enums.TipoProcedimento;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "T_PB_PROCEDIMENTO")
public class ProcedimentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PROCEDIMENTO")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "TP_TIPO_PROCEDIMENTO", nullable = false, length = 50)
    private TipoProcedimento tipo;

    @Column(name = "NM_NOME", nullable = false, length = 150)
    private String nome;

    @Column(name = "DS_DESCRICAO", length = 2000)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "ST_STATUS_PROCEDIMENTO", nullable = false, length = 50)
    private StatusProcedimento status;

    @Column(name = "DT_DATA_PREVISTA_INICIO", nullable = false)
    private LocalDateTime dataPrevistaInicio;

    @Column(name = "DT_DATA_PREVISTA_FIM", nullable = false)
    private LocalDateTime dataPrevistaFim;

    @Column(name = "AN_ANEXOS_URL", length = 500)
    private String anexosUrl;

    @Column(name = "OB_OBSERVACAO", length = 2000)
    private String observacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_REGISTRO_ATENDIMENTO", nullable = false)
    private RegistroAtendimentoEntity registroAtendimento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_ANIMAL", nullable = false)
    private AnimalEntity animal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_VETERINARIO", nullable = false)
    private VeterinarioEntity veterinario;

    @Column(name = "CA_CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "AT_UPDATED_AT")
    private LocalDateTime updatedAt;

    @PrePersist
    private void prePersist() { createdAt = LocalDateTime.now(); }

    @PreUpdate
    private void preUpdate() { updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TipoProcedimento getTipo() { return tipo; }
    public void setTipo(TipoProcedimento tipo) { this.tipo = tipo; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public StatusProcedimento getStatus() { return status; }
    public void setStatus(StatusProcedimento status) { this.status = status; }

    public LocalDateTime getDataPrevistaInicio() { return dataPrevistaInicio; }
    public void setDataPrevistaInicio(LocalDateTime dataPrevistaInicio) { this.dataPrevistaInicio = dataPrevistaInicio; }

    public LocalDateTime getDataPrevistaFim() { return dataPrevistaFim; }
    public void setDataPrevistaFim(LocalDateTime dataPrevistaFim) { this.dataPrevistaFim = dataPrevistaFim; }

    public String getAnexosUrl() { return anexosUrl; }
    public void setAnexosUrl(String anexosUrl) { this.anexosUrl = anexosUrl; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }

    public RegistroAtendimentoEntity getRegistroAtendimento() { return registroAtendimento; }
    public void setRegistroAtendimento(RegistroAtendimentoEntity registroAtendimento) { this.registroAtendimento = registroAtendimento; }

    public AnimalEntity getAnimal() { return animal; }
    public void setAnimal(AnimalEntity animal) { this.animal = animal; }

    public VeterinarioEntity getVeterinario() { return veterinario; }
    public void setVeterinario(VeterinarioEntity veterinario) { this.veterinario = veterinario; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
