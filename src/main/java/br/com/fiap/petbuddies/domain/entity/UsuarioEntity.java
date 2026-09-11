package br.com.fiap.petbuddies.domain.entity;

import br.com.fiap.petbuddies.domain.enums.identidade.PerfilUsuario;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.type.NumericBooleanConverter;
import java.time.LocalDateTime;

// Sao ids soltos, sem relacao JPA — exatamente um dos dois e preenchido, conforme o perfil.
@Entity
@Table(name = "T_PB_USUARIO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
    @Setter(AccessLevel.NONE)
    private LocalDateTime createdAt;

    @PrePersist
    private void prePersist() { createdAt = LocalDateTime.now(); }
}
