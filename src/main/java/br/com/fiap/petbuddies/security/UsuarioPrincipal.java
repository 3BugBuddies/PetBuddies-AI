package br.com.fiap.petbuddies.security;

import br.com.fiap.petbuddies.domain.entity.UsuarioEntity;
import br.com.fiap.petbuddies.domain.enums.PerfilUsuario;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/** Principal da sessao web, com o vinculo que {@link TokenService} ja usa no token da API. */
public class UsuarioPrincipal implements UserDetails {

    private final Long usuarioId;
    private final String login;
    private final String senhaHash;
    private final PerfilUsuario perfil;
    private final Long veterinarioId;
    private final Long responsavelId;
    private final boolean ativo;

    private UsuarioPrincipal(UsuarioEntity usuario) {
        this.usuarioId = usuario.getId();
        this.login = usuario.getLogin();
        this.senhaHash = usuario.getSenhaHash();
        this.perfil = usuario.getPerfil();
        this.veterinarioId = usuario.getVeterinarioId();
        this.responsavelId = usuario.getResponsavelId();
        this.ativo = usuario.isAtivo();
    }

    public static UsuarioPrincipal from(UsuarioEntity usuario) {
        return new UsuarioPrincipal(usuario);
    }

    public Long getUsuarioId() { return usuarioId; }

    public PerfilUsuario getPerfil() { return perfil; }

    public Long getVeterinarioId() { return veterinarioId; }

    public Long getResponsavelId() { return responsavelId; }

    public boolean isVet() { return perfil == PerfilUsuario.VET; }

    public boolean isTutor() { return perfil == PerfilUsuario.TUTOR; }

    @Override
    public List<GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + perfil.name()));
    }

    @Override
    public String getPassword() { return senhaHash; }

    @Override
    public String getUsername() { return login; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return ativo; }
}
