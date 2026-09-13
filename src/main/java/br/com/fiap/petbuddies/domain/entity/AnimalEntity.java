package br.com.fiap.petbuddies.domain.entity;

import br.com.fiap.petbuddies.domain.embeddable.Auditoria;
import br.com.fiap.petbuddies.domain.enums.cadastro.Especie;
import br.com.fiap.petbuddies.domain.enums.cadastro.Porte;
import br.com.fiap.petbuddies.domain.enums.cadastro.Sexo;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.type.NumericBooleanConverter;
import java.math.BigDecimal;
import java.time.LocalDate;

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

    @Embedded
    @Setter(AccessLevel.NONE)
    private Auditoria auditoria;

    @PrePersist
    private void prePersist() { auditoria = Auditoria.criadaAgora(); }

    @PreUpdate
    private void preUpdate() { auditoria = auditoria.atualizadaAgora(); }
}
