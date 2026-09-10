package br.com.fiap.petbuddies.dto;

import br.com.fiap.petbuddies.domain.entity.UsuarioEntity;
import br.com.fiap.petbuddies.domain.enums.identidade.PerfilUsuario;
import lombok.*;

/**
 * O corpo do login, exatamente como o contrato v2.1 §2 mostra: token, perfil,
 * usuario e os dois vinculos — um preenchido e o outro nulo.
 *
 * <p><b>Sem envelope HATEOAS.</b> Desde o PR #3 os recursos do Java vem em
 * {@code EntityModel}, mas um token nao e recurso: nao tem {@code self}, nao e
 * navegavel, e o contrato mostra o corpo cru. Nao existe assembler para ele.</p>
 *
 * <p>O vinculo nulo continua no JSON, como {@code null}: o app le os dois
 * campos e usa o que veio preenchido.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private PerfilUsuario perfil;
    private Long usuarioId;
    private Long responsavelId;
    private Long veterinarioId;

    public static LoginResponse from(UsuarioEntity usuario, String token) {
        LoginResponse dto = new LoginResponse();
        dto.token = token;
        dto.perfil = usuario.getPerfil();
        dto.usuarioId = usuario.getId();
        dto.responsavelId = usuario.getResponsavelId();
        dto.veterinarioId = usuario.getVeterinarioId();
        return dto;
    }
}
