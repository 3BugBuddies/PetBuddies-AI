package br.com.fiap.petbuddies.domain.entity;

import jakarta.persistence.*;
import org.hibernate.type.NumericBooleanConverter;
import java.time.LocalDateTime;

@Entity
@Table(name = "T_PB_VETERINARIO")
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

    public String getCrmv() { return crmv; }
    public void setCrmv(String crmv) { this.crmv = crmv; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public ClinicaEntity getClinica() { return clinica; }
    public void setClinica(ClinicaEntity clinica) { this.clinica = clinica; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
