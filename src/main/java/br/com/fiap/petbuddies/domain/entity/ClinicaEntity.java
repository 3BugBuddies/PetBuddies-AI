package br.com.fiap.petbuddies.domain.entity;

import br.com.fiap.petbuddies.domain.embeddable.Auditoria;
import br.com.fiap.petbuddies.domain.embeddable.Contato;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "T_PB_CLINICA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClinicaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CLINICA")
    private Long id;

    @Column(name = "NM_NOME_CLINICA", nullable = false, length = 150)
    private String nome;

    // UK_CLINICA_CNPJ
    @Column(name = "NR_CNPJ", nullable = false, unique = true, length = 14)
    private String cnpj;

    @Embedded
    private Contato contato;

    @Embedded
    @Setter(AccessLevel.NONE)
    private Auditoria auditoria;

    @PrePersist
    private void prePersist() { auditoria = Auditoria.criadaAgora(); }

    @PreUpdate
    private void preUpdate() { auditoria = auditoria.atualizadaAgora(); }
}
