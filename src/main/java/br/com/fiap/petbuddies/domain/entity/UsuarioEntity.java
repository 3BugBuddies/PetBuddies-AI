package br.com.fiap.petbuddies.domain.entity;

import br.com.fiap.petbuddies.domain.enums.PerfilUsuario;
import jakarta.persistence.*;
import org.hibernate.type.NumericBooleanConverter;
import java.time.LocalDateTime;

/**
 * Credencial de acesso, com o perfil e o vinculo com a identidade do registro.
 *
 * <p>Exatamente um dos dois ids de vinculo e preenchido, conforme o perfil: VET
 * aponta para o veterinario, TUTOR para o responsavel. Sao ids soltos, sem
 * relacao JPA — as tabelas de destino sao escritas pelo servico .NET, e a
 * leitura delas passa a existir no PR-J2, por projecao.</p>
 */
@Entity
@Table(name = "T_PB_USUARIO")
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_USUARIO")
    private Long id;

    @Column(name = "DS_LOGIN", nullable = false, unique = true, length = 120)
    private String login;

    @Column(name = "DS_SENHA_HASH", nullable = false, length = 100)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "TP_PERFIL", nullable = false, length = 20)
    private PerfilUsuario perfil;

    @Column(name = "ID_VETERINARIO")
    private Long veterinarioId;

    @Column(name = "ID_RESPONSAVEL")
    private Long responsavelId;

    @Column(name = "AT_ATIVO", nullable = false)

    @Convert(converter = NumericBooleanConverter.class)
    private boolean ativo = true;

    @Column(name = "CA_CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void prePersist() { createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }

    public PerfilUsuario getPerfil() { return perfil; }
    public void setPerfil(PerfilUsuario perfil) { this.perfil = perfil; }

    public Long getVeterinarioId() { return veterinarioId; }
    public void setVeterinarioId(Long veterinarioId) { this.veterinarioId = veterinarioId; }

    public Long getResponsavelId() { return responsavelId; }
    public void setResponsavelId(Long responsavelId) { this.responsavelId = responsavelId; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
