package br.com.fiap.petbuddies.domain.entity;

import br.com.fiap.petbuddies.domain.enums.TipoDado;
import br.com.fiap.petbuddies.domain.enums.TipoFonteValor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

// UK_CONDICAO_CLINICA_CODIGO (ID_CLINICA, CD_CODIGO): o código é único dentro
// da clínica, não globalmente.
@Entity
@Table(
    name = "T_PB_CONDICAO_CLINICA",
    uniqueConstraints = @UniqueConstraint(
        name = "UK_CONDICAO_CLINICA_CODIGO",
        columnNames = {"ID_CLINICA", "CD_CODIGO"})
)
public class CondicaoClinicaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CONDICAO_CLINICA")
    private Long id;

    @Column(name = "CD_CODIGO", nullable = false, length = 60)
    private String codigo;

    @Column(name = "DS_ROTULO", nullable = false, length = 255)
    private String rotulo;

    @Enumerated(EnumType.STRING)
    @Column(name = "TP_DADO", nullable = false, length = 20)
    private TipoDado tipoDado;

    @Enumerated(EnumType.STRING)
    @Column(name = "TP_FONTE_VALOR", nullable = false, length = 20)
    private TipoFonteValor fonteValor = TipoFonteValor.RELATO;

    @Column(name = "DS_UNIDADE", length = 20)
    private String unidade;

    @Column(name = "FL_CRITICA", nullable = false)
    private boolean critica;

    @Column(name = "AT_ATIVO", nullable = false)
    private boolean ativo = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_CLINICA", nullable = false)
    private ClinicaEntity clinica;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_VETERINARIO_AUTOR", nullable = false)
    private VeterinarioEntity autor;

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

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getRotulo() { return rotulo; }
    public void setRotulo(String rotulo) { this.rotulo = rotulo; }

    public TipoDado getTipoDado() { return tipoDado; }
    public void setTipoDado(TipoDado tipoDado) { this.tipoDado = tipoDado; }

    public TipoFonteValor getFonteValor() { return fonteValor; }
    public void setFonteValor(TipoFonteValor fonteValor) { this.fonteValor = fonteValor; }

    public String getUnidade() { return unidade; }
    public void setUnidade(String unidade) { this.unidade = unidade; }

    public boolean isCritica() { return critica; }
    public void setCritica(boolean critica) { this.critica = critica; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public ClinicaEntity getClinica() { return clinica; }
    public void setClinica(ClinicaEntity clinica) { this.clinica = clinica; }

    public VeterinarioEntity getAutor() { return autor; }
    public void setAutor(VeterinarioEntity autor) { this.autor = autor; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
