package br.com.fiap.petbuddies.domain.entity;

import br.com.fiap.petbuddies.domain.embeddable.Auditoria;
import br.com.fiap.petbuddies.domain.embeddable.FaixaDose;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;

import java.time.LocalDate;

// Imutavel por contrato — corrigir e emitir nova prescricao, nao editar esta; sem PUT/DELETE no controller.
@Entity
@Immutable
@Table(name = "T_PB_PRESCRICAO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrescricaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PRESCRICAO")
    private Long id;

    @Column(name = "NM_MEDICAMENTO", nullable = false, length = 150)
    private String medicamento;

    @Embedded
    private FaixaDose faixaDose;

    @Column(name = "NR_FREQUENCIA_DIA", nullable = false)
    private Integer frequenciaDia;

    @Column(name = "NR_DURACAO_DIAS", nullable = false)
    private Integer duracaoDias;

    @Column(name = "DT_INICIO", nullable = false)
    private LocalDate dataInicio;

    // CLOB: sem @Lob o Hibernate declara VARCHAR2(255) e o validate recusa a subida.
    @Lob
    @Column(name = "TX_ORIENTACAO")
    private String orientacao;

    // Nasce nulo, sem FK.
    @Column(name = "ID_MATERIAL_ORIGEM")
    private Long materialOrigemId;

    @Column(name = "NR_VERSAO_ORIGEM")
    private Integer versaoOrigem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_ANIMAL", nullable = false)
    private AnimalEntity animal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_VETERINARIO", nullable = false)
    private VeterinarioEntity veterinario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_REGISTRO_ATENDIMENTO", nullable = false)
    private RegistroAtendimentoEntity registroAtendimento;

    @Embedded
    @Setter(AccessLevel.NONE)
    private Auditoria auditoria;

    @PrePersist
    private void prePersist() { auditoria = Auditoria.criadaAgora(); }
}
