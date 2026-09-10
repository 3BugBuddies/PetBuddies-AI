package br.com.fiap.petbuddies.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "T_PB_REGISTRO_ATENDIMENTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistroAtendimentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_REGISTRO_ATENDIMENTO")
    private Long id;

    @Column(name = "DT_DATA_ATENDIMENTO", nullable = false)
    private LocalDateTime dataAtendimento;

    @Column(name = "AN_ANAMNESE", length = 2000)
    private String anamnese;

    @Column(name = "DG_DIAGNOSTICO", length = 2000)
    private String diagnostico;

    @Column(name = "TR_TRATAMENTO", length = 2000)
    private String tratamento;

    @Column(name = "OB_OBSERVACAO", length = 2000)
    private String observacao;

    @Column(name = "PR_PROXIMO_RETORNO")
    private LocalDate proximoRetorno;

    @Column(name = "PR_PROXIMA_VACINA")
    private LocalDate proximaVacina;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_ANIMAL", nullable = false)
    private AnimalEntity animal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_CONSULTA", nullable = false)
    private ConsultaEntity consulta;

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
