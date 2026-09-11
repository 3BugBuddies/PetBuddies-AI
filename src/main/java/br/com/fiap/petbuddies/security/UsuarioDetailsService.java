package br.com.fiap.petbuddies.security;

import br.com.fiap.petbuddies.domain.entity.UsuarioEntity;
import br.com.fiap.petbuddies.domain.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Cadeia da API nao passa por aqui: o token ja carrega perfil e vinculo — consultar o banco a cada requisicao anularia o stateless.
// Devolve UsuarioPrincipal, nao o User padrao — carrega veterinarioId/responsavelId para os controllers de pagina.
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

        return UsuarioPrincipal.from(usuario);
    }
}
