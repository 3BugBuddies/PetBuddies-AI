package br.com.fiap.petbuddies.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * A clinica. Raiz do registro: nada de veterinario nem de condicao clinica
 * existe antes dela (ADR s3-16).
 *
 * <p>O CNPJ e a identidade externa da clinica e nao repete: o DDL garante por
 * {@code UK_CLINICA_CNPJ}, e o servico recusa antes, com conflito de dominio,
 * para que a violacao chegue como 409 e nao como erro de driver.</p>
 *
 * <p>Absorvida pelo Java no PR-J11 (ADR s3-25), quando o registro deixou de ser
 * lido por projecao e passou a ser propriedade deste servico.</p>
 */
@Entity
@Table(name = "T_PB_CLINICA")
public class ClinicaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CLINICA")
    private Long id;

    @Column(name = "NM_NOME_CLINICA", nullable = false, length = 150)
    private String nome;

    @Column(name = "NR_CNPJ", nullable = false, unique = true, length = 14)
    private String cnpj;

    @Column(name = "TL_TELEFONE", nullable = false, length = 20)
    private String telefone;

    @Column(name = "EM_EMAIL", length = 254)
    private String email;

    @Column(name = "CA_CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "AT_UPDATED_AT")
    private LocalDateTime updatedAt;

    @PrePersist
    private void prePersist() { createdAt = LocalDateTime.now(); }

    @PreUpdate
    private void preUpdate() { updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
