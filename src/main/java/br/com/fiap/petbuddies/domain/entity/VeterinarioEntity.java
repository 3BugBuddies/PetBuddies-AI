package br.com.fiap.petbuddies.domain.entity;

import br.com.fiap.petbuddies.domain.embeddable.Auditoria;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.type.NumericBooleanConverter;

@Entity
@Table(name = "T_PB_VETERINARIO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VeterinarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_VETERINARIO")
    private Long id;

    @Column(name = "NM_NOME_VETERINARIO", nullable = false, length = 150)
    private String nome;

    // UK_VETERINARIO_CRMV
    @Column(name = "NR_CRMV", nullable = false, unique = true, length = 30)
    private String crmv;

    @Column(name = "EM_EMAIL", length = 254)
    private String email;

    @Column(name = "AT_ATIVO", nullable = false)

    @Convert(converter = NumericBooleanConverter.class)
    private boolean ativo = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_CLINICA", nullable = false)
    private ClinicaEntity clinica;

    @Embedded
    @Setter(AccessLevel.NONE)
    private Auditoria auditoria;

    @PrePersist
    private void prePersist() { auditoria = Auditoria.criadaAgora(); }

    @PreUpdate
    private void preUpdate() { auditoria = auditoria.atualizadaAgora(); }
}
