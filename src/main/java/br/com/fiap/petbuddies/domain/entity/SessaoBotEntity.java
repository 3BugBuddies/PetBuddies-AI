package br.com.fiap.petbuddies.domain.entity;

import br.com.fiap.petbuddies.domain.enums.Intencao;
import br.com.fiap.petbuddies.domain.enums.AcaoPendente;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "T_PB_SESSAO_BOT")
public class SessaoBotEntity {

    private static final ZoneId ZONE_ID = ZoneId.of("America/Sao_Paulo");

    @Id
    @Column(name = "CD_TELEFONE", length = 20, nullable = false)
    private String telefone;

    @Enumerated(EnumType.STRING)
    @Column(name = "DS_FLUXO_ATIVO", length = 30)
    private Intencao fluxoAtivo;

    @Column(name = "DT_UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "DS_STAGE_ATUAL", length = 60)
    private String stageAtual;

    @Column(name = "TX_DADOS_COLETADOS", length = 2000)
    private String dadosColetados;

    @Enumerated(EnumType.STRING)
    @Column(name = "DS_ACAO_PENDENTE", length = 60)
    private AcaoPendente acaoPendente;

    @Lob
    @Column(name = "TX_DADOS_PENDENTES")
    private String dadosPendentesJson;

    public SessaoBotEntity() {}

    public SessaoBotEntity(String telefone, Intencao fluxoAtivo, LocalDateTime updatedAt) {
        this.telefone = telefone;
        this.fluxoAtivo = fluxoAtivo;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    @PreUpdate
    private void atualizarTimestamp() {
        updatedAt = LocalDateTime.now(ZONE_ID);
    }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public Intencao getFluxoAtivo() { return fluxoAtivo; }
    public void setFluxoAtivo(Intencao fluxoAtivo) { this.fluxoAtivo = fluxoAtivo; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getStageAtual() { return stageAtual; }
    public void setStageAtual(String stageAtual) { this.stageAtual = stageAtual; }

    public String getDadosColetados() { return dadosColetados; }
    public void setDadosColetados(String dadosColetados) { this.dadosColetados = dadosColetados; }

    public AcaoPendente getAcaoPendente() { return acaoPendente; }
    public void setAcaoPendente(AcaoPendente acaoPendente) { this.acaoPendente = acaoPendente; }

    public String getDadosPendentesJson() { return dadosPendentesJson; }
    public void setDadosPendentesJson(String dadosPendentesJson) { this.dadosPendentesJson = dadosPendentesJson; }

}
