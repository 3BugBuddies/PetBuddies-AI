package br.com.fiap.petbuddies.dto.identidade;

import br.com.fiap.petbuddies.domain.entity.UsuarioEntity;
import br.com.fiap.petbuddies.domain.enums.identidade.PerfilUsuario;
import lombok.*;

// Sem envelope HATEOAS — token nao e recurso, nao tem self; nao existe assembler para ele.
// O vinculo nulo continua no JSON, como null — o app le os dois campos e usa o que veio preenchido.
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
    private String nome;

    public static LoginResponse from(UsuarioEntity usuario, String token) {
        return from(usuario, token, null);
    }

    public static LoginResponse from(UsuarioEntity usuario, String token, String nome) {
        LoginResponse dto = new LoginResponse();
        dto.token = token;
        dto.perfil = usuario.getPerfil();
        dto.usuarioId = usuario.getId();
        dto.responsavelId = usuario.getResponsavelId();
        dto.veterinarioId = usuario.getVeterinarioId();
        dto.nome = nome;
        return dto;
    }
}
