package br.com.fiap.petbuddies.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * O tutor. Sem CPF, sem data de nascimento e sem endereco: o cadastro do app nao
 * pede (decisao I).
 *
 * <p>NENHUM CAMPO E UNICO AQUI. O telefone e obrigatorio, mas repete — dois
 * tutores da mesma casa compartilham o numero, e o DDL nao declara restricao
 * alguma sobre ele. O e-mail e opcional pelo mesmo motivo: o cadastro nasce no
 * balcao da clinica, onde nem todo tutor tem e-mail a informar.</p>
 *
 * <p>Absorvido pelo Java no PR-J11 (ADR s3-25).</p>
 */
@Entity
@Table(name = "T_PB_RESPONSAVEL")
public class ResponsavelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_RESPONSAVEL")
    private Long id;

    @Column(name = "NM_NOME_RESPONSAVEL", nullable = false, length = 150)
    private String nome;

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

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
