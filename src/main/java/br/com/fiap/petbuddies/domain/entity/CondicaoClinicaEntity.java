package br.com.fiap.petbuddies.domain.entity;

import br.com.fiap.petbuddies.domain.embeddable.Auditoria;
import br.com.fiap.petbuddies.domain.enums.prescricao.TipoDado;
import br.com.fiap.petbuddies.domain.enums.prescricao.TipoFonteValor;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.type.NumericBooleanConverter;

// UK_CONDICAO_CLINICA_CODIGO (ID_CLINICA, CD_CODIGO): o código é único dentro
// da clínica, não globalmente.
@Entity
@Table(
    name = "T_PB_CONDICAO_CLINICA",
    uniqueConstraints = @UniqueConstraint(
        name = "UK_CONDICAO_CLINICA_CODIGO",
        columnNames = {"ID_CLINICA", "CD_CODIGO"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    @Convert(converter = NumericBooleanConverter.class)
    private boolean critica;

    @Column(name = "AT_ATIVO", nullable = false)

    @Convert(converter = NumericBooleanConverter.class)
    private boolean ativo = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_CLINICA", nullable = false)
    private ClinicaEntity clinica;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_VETERINARIO_AUTOR", nullable = false)
    private VeterinarioEntity autor;

    @Embedded
    @Setter(AccessLevel.NONE)
    private Auditoria auditoria;

    @PrePersist
    private void prePersist() { auditoria = Auditoria.criadaAgora(); }

    @PreUpdate
    private void preUpdate() { auditoria = auditoria.atualizadaAgora(); }
}
