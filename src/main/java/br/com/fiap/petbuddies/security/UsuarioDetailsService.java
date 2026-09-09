package br.com.fiap.petbuddies.security;

import br.com.fiap.petbuddies.domain.entity.UsuarioEntity;
import br.com.fiap.petbuddies.domain.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Carrega o usuario pelo login para o <b>formulario da web</b> — a cadeia de
 * sessao. A cadeia da API nao passa por aqui: o token ja carrega perfil e
 * vinculo, e consultar o banco a cada requisicao anularia o ganho de ser
 * stateless.
 *
 * <p>O perfil vira papel pelo {@code roles(...)}, que prefixa {@code ROLE_}
 * sozinho — e o que faz {@code hasRole("VET")} casar na segunda cadeia.</p>
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String login) {
        UsuarioEntity usuario = usuarioRepository.findByLoginAndAtivoTrue(login)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciais invalidas."));

        return User.withUsername(usuario.getLogin())
                .password(usuario.getSenhaHash())
                .roles(usuario.getPerfil().name())
                .build();
    }
}
