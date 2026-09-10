package br.com.fiap.petbuddies.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.type.NumericBooleanConverter;
import java.time.LocalDateTime;

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
