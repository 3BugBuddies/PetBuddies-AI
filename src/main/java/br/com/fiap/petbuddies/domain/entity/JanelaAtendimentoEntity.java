package br.com.fiap.petbuddies.domain.entity;

import br.com.fiap.petbuddies.domain.embeddable.Auditoria;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "T_PB_JANELA_ATENDIMENTO",
    uniqueConstraints = @UniqueConstraint(
        name = "UK_JANELA_VET_INICIO",
        columnNames = {"ID_VETERINARIO", "DH_DATA_HORA_INICIO"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JanelaAtendimentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_JANELA_ATENDIMENTO")
    private Long id;

    @Column(name = "DH_DATA_HORA_INICIO", nullable = false)
    private LocalDateTime dataHoraInicio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_VETERINARIO", nullable = false)
    private VeterinarioEntity veterinario;

    // Nula = horário livre. FK_JANELA_CONSULTA é ON DELETE SET NULL; cancelamento
    // de consulta não é exclusão e precisa apagar o vínculo à mão (dívida conhecida).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CONSULTA")
    private ConsultaEntity consulta;

    @Embedded
    @Setter(AccessLevel.NONE)
    private Auditoria auditoria;

    @PrePersist
    private void prePersist() { auditoria = Auditoria.criadaAgora(); }

    @PreUpdate
    private void preUpdate() { auditoria = auditoria.atualizadaAgora(); }
}
