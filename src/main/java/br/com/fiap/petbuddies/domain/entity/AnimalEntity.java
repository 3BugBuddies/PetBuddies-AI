package br.com.fiap.petbuddies.domain.entity;

import br.com.fiap.petbuddies.domain.enums.Especie;
import br.com.fiap.petbuddies.domain.enums.Porte;
import br.com.fiap.petbuddies.domain.enums.Sexo;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.type.NumericBooleanConverter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "T_PB_ANIMAL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
    @Setter(AccessLevel.NONE)
    private LocalDateTime createdAt;

    @Column(name = "AT_UPDATED_AT")
    @Setter(AccessLevel.NONE)
    private LocalDateTime updatedAt;

    @PrePersist
    private void prePersist() { createdAt = LocalDateTime.now(); }

    @PreUpdate
    private void preUpdate() { updatedAt = LocalDateTime.now(); }
}
