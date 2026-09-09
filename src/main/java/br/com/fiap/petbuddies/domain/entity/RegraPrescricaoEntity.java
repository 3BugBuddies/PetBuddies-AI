package br.com.fiap.petbuddies.domain.entity;

import br.com.fiap.petbuddies.domain.enums.OperadorRegra;
import br.com.fiap.petbuddies.domain.enums.TipoAcaoRegra;
import br.com.fiap.petbuddies.domain.enums.TipoDado;
import br.com.fiap.petbuddies.domain.enums.TipoFonteValor;
import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Condição → ação sobre a dose de uma prescrição. Imutável como a prescrição
 * que a carrega — regra assinada não se corrige, se substitui numa nova
 * prescrição.
 *
 * <p>O rótulo, o tipo de dado e a fonte são copiados da {@link CondicaoClinicaEntity}
 * no momento da criação e nunca mais mudam (ADR s3-10), mesmo que o catálogo
 * mude depois — é o que permite o check-in avaliar a regra sem consultar o
 * catálogo.</p>
 */
@Entity
@Immutable
@Table(name = "T_PB_REGRA_PRESCRICAO")
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

    @Column(name = "DS_ROTULO_CONGELADO", nullable = false, length = 255)
    private String rotuloCongelado;

    @Enumerated(EnumType.STRING)
    @Column(name = "TP_DADO_CONGELADO", nullable = false, length = 20)
    private TipoDado tipoDadoCongelado;

    @Enumerated(EnumType.STRING)
    @Column(name = "TP_FONTE_VALOR_CONGELADA", nullable = false, length = 20)
    private TipoFonteValor fonteValorCongelada;

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

    @Column(name = "CA_CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "AT_UPDATED_AT")
    private LocalDateTime updatedAt;

    @PrePersist
    private void prePersist() { createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PrescricaoEntity getPrescricao() { return prescricao; }
    public void setPrescricao(PrescricaoEntity prescricao) { this.prescricao = prescricao; }

    public CondicaoClinicaEntity getCondicaoClinica() { return condicaoClinica; }
    public void setCondicaoClinica(CondicaoClinicaEntity condicaoClinica) { this.condicaoClinica = condicaoClinica; }

    public String getRotuloCongelado() { return rotuloCongelado; }
    public void setRotuloCongelado(String rotuloCongelado) { this.rotuloCongelado = rotuloCongelado; }

    public TipoDado getTipoDadoCongelado() { return tipoDadoCongelado; }
    public void setTipoDadoCongelado(TipoDado tipoDadoCongelado) { this.tipoDadoCongelado = tipoDadoCongelado; }

    public TipoFonteValor getFonteValorCongelada() { return fonteValorCongelada; }
    public void setFonteValorCongelada(TipoFonteValor fonteValorCongelada) { this.fonteValorCongelada = fonteValorCongelada; }

    public OperadorRegra getOperador() { return operador; }
    public void setOperador(OperadorRegra operador) { this.operador = operador; }

    public BigDecimal getLimite() { return limite; }
    public void setLimite(BigDecimal limite) { this.limite = limite; }

    public TipoAcaoRegra getAcaoDose() { return acaoDose; }
    public void setAcaoDose(TipoAcaoRegra acaoDose) { this.acaoDose = acaoDose; }

    public Integer getOrdem() { return ordem; }
    public void setOrdem(Integer ordem) { this.ordem = ordem; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
