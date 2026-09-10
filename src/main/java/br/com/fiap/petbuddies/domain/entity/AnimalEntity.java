package br.com.fiap.petbuddies.domain.entity;

import br.com.fiap.petbuddies.domain.enums.Especie;
import br.com.fiap.petbuddies.domain.enums.Porte;
import br.com.fiap.petbuddies.domain.enums.Sexo;
import jakarta.persistence.*;
import org.hibernate.type.NumericBooleanConverter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "T_PB_ANIMAL")
public class AnimalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ANIMAL")
    private Long id;

    @Column(name = "NM_NOME_ANIMAL", nullable = false, length = 150)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "ES_ESPECIE", nullable = false, length = 50)
    private Especie especie;

    @Column(name = "RC_RACA", nullable = false, length = 100)
    private String raca = "SEM_RACA";

    @Enumerated(EnumType.STRING)
    @Column(name = "PT_PORTE", nullable = false, length = 50)
    private Porte porte;

    @Enumerated(EnumType.STRING)
    @Column(name = "SX_SEXO", nullable = false, length = 50)
    private Sexo sexo;

    @Column(name = "DT_DATA_NASCIMENTO", nullable = false)
    private LocalDate dataNascimento;

    @Column(name = "NR_PESO", precision = 5, scale = 2)
    private BigDecimal peso;

    @Column(name = "CN_CONDICAO_CRONICA", nullable = false)

    @Convert(converter = NumericBooleanConverter.class)
    private boolean condicaoCronica;

    @Column(name = "CT_CASTRADO", nullable = false)

    @Convert(converter = NumericBooleanConverter.class)
    private boolean castrado;

    @Column(name = "FT_FOTO", length = 500)
    private String foto;

    @Column(name = "OB_ALERGIA", length = 2000)
    private String alergias;

    @Column(name = "OB_OBSERVACOES", length = 2000)
    private String observacoes;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_RESPONSAVEL", nullable = false)
    private ResponsavelEntity responsavel;

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

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Especie getEspecie() { return especie; }
    public void setEspecie(Especie especie) { this.especie = especie; }

    public String getRaca() { return raca; }
    public void setRaca(String raca) { this.raca = raca; }

    public Porte getPorte() { return porte; }
    public void setPorte(Porte porte) { this.porte = porte; }

    public Sexo getSexo() { return sexo; }
    public void setSexo(Sexo sexo) { this.sexo = sexo; }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }

    public BigDecimal getPeso() { return peso; }
    public void setPeso(BigDecimal peso) { this.peso = peso; }

    public boolean isCondicaoCronica() { return condicaoCronica; }
    public void setCondicaoCronica(boolean condicaoCronica) { this.condicaoCronica = condicaoCronica; }

    public boolean isCastrado() { return castrado; }
    public void setCastrado(boolean castrado) { this.castrado = castrado; }

    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }

    public String getAlergias() { return alergias; }
    public void setAlergias(String alergias) { this.alergias = alergias; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public ResponsavelEntity getResponsavel() { return responsavel; }
    public void setResponsavel(ResponsavelEntity responsavel) { this.responsavel = responsavel; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
