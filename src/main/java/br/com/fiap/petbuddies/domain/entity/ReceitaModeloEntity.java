package br.com.fiap.petbuddies.domain.entity;

import br.com.fiap.petbuddies.domain.embeddable.Auditoria;
import br.com.fiap.petbuddies.domain.embeddable.FaixaDose;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.type.NumericBooleanConverter;

import java.util.ArrayList;
import java.util.List;

// UK_RECEITA_MODELO_TITULO (ID_CLINICA, NM_TITULO): vale também para as desativadas.
@Entity
@Table(
    name = "T_PB_RECEITA_MODELO",
    uniqueConstraints = @UniqueConstraint(
        name = "UK_RECEITA_MODELO_TITULO",
        columnNames = {"ID_CLINICA", "NM_TITULO"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReceitaModeloEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_RECEITA_MODELO")
    private Long id;

    @Column(name = "NM_TITULO", nullable = false, length = 120)
    private String titulo;

    @Column(name = "NM_MEDICAMENTO", nullable = false, length = 150)
    private String medicamento;

    @Embedded
    private FaixaDose faixaDose;

    @Column(name = "NR_FREQUENCIA_DIA", nullable = false)
    private Integer frequenciaDia;

    @Column(name = "NR_DURACAO_DIAS", nullable = false)
    private Integer duracaoDias;

    // CLOB: sem @Lob o Hibernate declara VARCHAR2(255) e o validate recusa a subida.
    @Lob
    @Column(name = "TX_ORIENTACAO")
    private String orientacao;

    @Column(name = "NR_VERSAO", nullable = false)
    private Integer versao = 1;

    @Column(name = "AT_ATIVO", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private boolean ativo = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_CLINICA", nullable = false)
    private ClinicaEntity clinica;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_VETERINARIO_AUTOR", nullable = false)
    private VeterinarioEntity autor;

    @OneToMany(mappedBy = "receitaModelo", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem ASC")
    private List<RegraReceitaModeloEntity> regras = new ArrayList<>();

    @Embedded
    @Setter(AccessLevel.NONE)
    private Auditoria auditoria;

    public void substituirRegras(List<RegraReceitaModeloEntity> novas) {
        regras.clear();
        novas.forEach(regra -> regra.setReceitaModelo(this));
        regras.addAll(novas);
    }

    @PrePersist
    private void prePersist() { auditoria = Auditoria.criadaAgora(); }

    @PreUpdate
    private void preUpdate() { auditoria = auditoria.atualizadaAgora(); }
}
