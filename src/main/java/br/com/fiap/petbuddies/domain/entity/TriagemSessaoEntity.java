package br.com.fiap.petbuddies.domain.entity;

import br.com.fiap.petbuddies.domain.enums.ClassificacaoTriagem;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "T_PB_TRIAGEM_SESSAO")
public class TriagemSessaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_pb_triagem_sessao")
    @SequenceGenerator(name = "seq_pb_triagem_sessao", sequenceName = "SEQ_T_PB_TRIAGEM_SESSAO", allocationSize = 1)
    @Column(name = "ID_TRIAGEM_SESSAO")
    private Long id;

    @Column(name = "NR_TELEFONE", nullable = false)
    private String telefone;

    @Column(name = "ID_PET_NET_ANIMAL")
    private Long petNetApiAnimalId;

    @Column(name = "ID_PET_NET_RESPONSAVEL")
    private Long petNetApiResponsavelId;

    @Column(name = "TX_SINTOMA_PRINCIPAL", nullable = false, length = 500)
    private String sintomaPrincipal;

    @Enumerated(EnumType.STRING)
    @Column(name = "TP_CLASSIFICACAO")
    private ClassificacaoTriagem classificacao;

    @Column(name = "NR_SCORE_TRIAGEM")
    private Integer scoreTriagem;

    @Column(name = "TX_RECOMENDACAO", length = 1000)
    private String recomendacao;

    @Column(name = "BL_ALERTA_ENVIADO", nullable = false)
    private Boolean alertaEnviado = false;

    @Column(name = "DT_INICIADA_EM", nullable = false, updatable = false)
    private LocalDateTime iniciadaEm;

    @Column(name = "DT_FINALIZADA_EM")
    private LocalDateTime finalizadaEm;

    @PrePersist
    private void prePersist() { iniciadaEm = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public Long getPetNetApiAnimalId() { return petNetApiAnimalId; }
    public void setPetNetApiAnimalId(Long petNetApiAnimalId) { this.petNetApiAnimalId = petNetApiAnimalId; }
    public Long getPetNetApiResponsavelId() { return petNetApiResponsavelId; }
    public void setPetNetApiResponsavelId(Long petNetApiResponsavelId) { this.petNetApiResponsavelId = petNetApiResponsavelId; }
    public String getSintomaPrincipal() { return sintomaPrincipal; }
    public void setSintomaPrincipal(String sintomaPrincipal) { this.sintomaPrincipal = sintomaPrincipal; }
    public ClassificacaoTriagem getClassificacao() { return classificacao; }
    public void setClassificacao(ClassificacaoTriagem classificacao) { this.classificacao = classificacao; }
    public Integer getScoreTriagem() { return scoreTriagem; }
    public void setScoreTriagem(Integer scoreTriagem) { this.scoreTriagem = scoreTriagem; }
    public String getRecomendacao() { return recomendacao; }
    public void setRecomendacao(String recomendacao) { this.recomendacao = recomendacao; }
    public Boolean getAlertaEnviado() { return alertaEnviado; }
    public void setAlertaEnviado(Boolean alertaEnviado) { this.alertaEnviado = alertaEnviado; }
    public LocalDateTime getIniciadaEm() { return iniciadaEm; }
    public LocalDateTime getFinalizadaEm() { return finalizadaEm; }
    public void setFinalizadaEm(LocalDateTime finalizadaEm) { this.finalizadaEm = finalizadaEm; }
}
