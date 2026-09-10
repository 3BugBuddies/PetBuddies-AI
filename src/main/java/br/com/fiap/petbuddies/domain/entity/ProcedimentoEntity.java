package br.com.fiap.petbuddies.domain.entity;

import br.com.fiap.petbuddies.domain.enums.atendimento.StatusProcedimento;
import br.com.fiap.petbuddies.domain.enums.atendimento.TipoProcedimento;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "T_PB_PROCEDIMENTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcedimentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PROCEDIMENTO")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "TP_TIPO_PROCEDIMENTO", nullable = false, length = 50)
    private TipoProcedimento tipo;

    @Column(name = "NM_NOME", nullable = false, length = 150)
    private String nome;

    @Column(name = "DS_DESCRICAO", length = 2000)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "ST_STATUS_PROCEDIMENTO", nullable = false, length = 50)
    private StatusProcedimento status;

    @Column(name = "DT_DATA_PREVISTA_INICIO", nullable = false)
    private LocalDateTime dataPrevistaInicio;

    @Column(name = "DT_DATA_PREVISTA_FIM", nullable = false)
    private LocalDateTime dataPrevistaFim;

    @Column(name = "AN_ANEXOS_URL", length = 500)
    private String anexosUrl;

    @Column(name = "OB_OBSERVACAO", length = 2000)
    private String observacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_REGISTRO_ATENDIMENTO", nullable = false)
    private RegistroAtendimentoEntity registroAtendimento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_ANIMAL", nullable = false)
    private AnimalEntity animal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_VETERINARIO", nullable = false)
    private VeterinarioEntity veterinario;

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
