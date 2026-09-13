package br.com.fiap.petbuddies.domain.entity;

import br.com.fiap.petbuddies.domain.embeddable.Auditoria;
import br.com.fiap.petbuddies.domain.embeddable.Contato;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "T_PB_RESPONSAVEL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResponsavelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_RESPONSAVEL")
    private Long id;

    @Column(name = "NM_NOME_RESPONSAVEL", nullable = false, length = 150)
    private String nome;

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
