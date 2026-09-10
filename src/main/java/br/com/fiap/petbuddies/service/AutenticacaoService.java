package br.com.fiap.petbuddies.service;

import br.com.fiap.petbuddies.domain.entity.UsuarioEntity;
import br.com.fiap.petbuddies.domain.repository.UsuarioRepository;
import br.com.fiap.petbuddies.domain.repository.VeterinarioRepository;
import br.com.fiap.petbuddies.dto.LoginRequest;
import br.com.fiap.petbuddies.dto.LoginResponse;
import br.com.fiap.petbuddies.exception.identidade.CredenciaisInvalidasException;
import br.com.fiap.petbuddies.security.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Confere a credencial e emite o token.
 *
 * <p>Os tres modos de recusa — login inexistente, senha errada e usuario
 * inativo — terminam na <b>mesma</b> excecao, e portanto na mesma resposta. O
 * filtro por ativo esta na propria consulta, entao inativo ja chega aqui como
 * vazio.</p>
 *
 * <p>Nada de senha em log: nem o valor recebido, nem o hash, nem o corpo da
 * requisicao.</p>
 */
@Service
public class AutenticacaoService {

    private final UsuarioRepository usuarioRepository;
    private final VeterinarioRepository veterinarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AutenticacaoService(
            UsuarioRepository usuarioRepository,
            VeterinarioRepository veterinarioRepository,
            PasswordEncoder passwordEncoder,
            TokenService tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.veterinarioRepository = veterinarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public LoginResponse autenticar(LoginRequest request) {
        UsuarioEntity usuario = usuarioRepository.findByLoginAndAtivoTrue(request.getLogin())
                .orElseThrow(CredenciaisInvalidasException::new);

        if (!passwordEncoder.matches(request.getSenha(), usuario.getSenhaHash())) {
            throw new CredenciaisInvalidasException();
        }
        return LoginResponse.from(usuario, tokenService.emitir(usuario, resolverClinica(usuario)));
    }

    // Fica aqui e nao no TokenService, que nao le repositorio. TUTOR devolve
    // null, e a claim sai ausente.
    private Long resolverClinica(UsuarioEntity usuario) {
        if (usuario.getVeterinarioId() == null) {
            return null;
        }
        return veterinarioRepository.findClinicaIdById(usuario.getVeterinarioId()).orElse(null);
    }
}
