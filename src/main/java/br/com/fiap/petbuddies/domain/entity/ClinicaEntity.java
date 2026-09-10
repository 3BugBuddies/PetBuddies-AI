package br.com.fiap.petbuddies.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

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

    @Column(name = "TL_TELEFONE", nullable = false, length = 20)
    private String telefone;

    @Column(name = "EM_EMAIL", length = 254)
    private String email;

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
