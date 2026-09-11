package br.com.fiap.petbuddies.service.identidade;

import br.com.fiap.petbuddies.domain.entity.ClinicaEntity;
import br.com.fiap.petbuddies.domain.entity.ResponsavelEntity;
import br.com.fiap.petbuddies.domain.entity.UsuarioEntity;
import br.com.fiap.petbuddies.domain.entity.VeterinarioEntity;
import br.com.fiap.petbuddies.domain.enums.identidade.PerfilUsuario;
import br.com.fiap.petbuddies.domain.repository.ClinicaRepository;
import br.com.fiap.petbuddies.domain.repository.ResponsavelRepository;
import br.com.fiap.petbuddies.domain.repository.UsuarioRepository;
import br.com.fiap.petbuddies.domain.repository.VeterinarioRepository;
import br.com.fiap.petbuddies.dto.identidade.LoginResponse;
import br.com.fiap.petbuddies.dto.identidade.RegistroRequest;
import br.com.fiap.petbuddies.dto.identidade.TrocaSenhaRequest;
import br.com.fiap.petbuddies.exception.cadastro.ClinicaNaoProvisionadaException;
import br.com.fiap.petbuddies.exception.cadastro.CrmvDuplicadoException;
import br.com.fiap.petbuddies.exception.cadastro.EmailResponsavelDuplicadoException;
import br.com.fiap.petbuddies.exception.cadastro.LoginDuplicadoException;
import br.com.fiap.petbuddies.exception.cadastro.RegistroIncompletoException;
import br.com.fiap.petbuddies.exception.cadastro.TelefoneResponsavelDuplicadoException;
import br.com.fiap.petbuddies.exception.identidade.CredenciaisInvalidasException;
import br.com.fiap.petbuddies.security.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class RegistroService {

    private final UsuarioRepository usuarioRepository;
    private final ResponsavelRepository responsavelRepository;
    private final VeterinarioRepository veterinarioRepository;
    private final ClinicaRepository clinicaRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public RegistroService(
            UsuarioRepository usuarioRepository,
            ResponsavelRepository responsavelRepository,
            VeterinarioRepository veterinarioRepository,
            ClinicaRepository clinicaRepository,
            PasswordEncoder passwordEncoder,
            TokenService tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.responsavelRepository = responsavelRepository;
        this.veterinarioRepository = veterinarioRepository;
        this.clinicaRepository = clinicaRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional
    public LoginResponse registrar(RegistroRequest request) {
        return switch (request.getTipo()) {
            case TUTOR -> registrarTutor(request);
            case VET -> registrarVeterinario(request);
        };
    }

    // O subject do token e o id do usuario, nao o login (TokenService.emitir).
    @Transactional
    public void trocarSenha(Long usuarioId, TrocaSenhaRequest request) {
        UsuarioEntity usuario = usuarioRepository.findById(usuarioId)
                .filter(UsuarioEntity::isAtivo)
                .orElseThrow(CredenciaisInvalidasException::new);

        if (!passwordEncoder.matches(request.getSenhaAtual(), usuario.getSenhaHash())) {
            throw new CredenciaisInvalidasException();
        }
        usuario.setSenhaHash(passwordEncoder.encode(request.getSenhaNova()));
        usuarioRepository.save(usuario);
    }

    private LoginResponse registrarTutor(RegistroRequest request) {
        if (!StringUtils.hasText(request.getTelefone())) {
            throw new RegistroIncompletoException("Telefone é obrigatório para o tipo TUTOR.");
        }
        garantirLoginLivre(request.getEmail());
        if (responsavelRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new EmailResponsavelDuplicadoException(request.getEmail());
        }
        if (responsavelRepository.existsByTelefone(request.getTelefone())) {
            throw new TelefoneResponsavelDuplicadoException(request.getTelefone());
        }

        ResponsavelEntity responsavel = new ResponsavelEntity();
        responsavel.setNome(request.getNome());
        responsavel.setTelefone(request.getTelefone());
        responsavel.setEmail(request.getEmail());
        responsavelRepository.save(responsavel);

        UsuarioEntity usuario = novoUsuario(request, PerfilUsuario.TUTOR);
        usuario.setResponsavelId(responsavel.getId());
        usuarioRepository.save(usuario);

        return LoginResponse.from(usuario, tokenService.emitir(usuario, null), responsavel.getNome());
    }

    private LoginResponse registrarVeterinario(RegistroRequest request) {
        if (!StringUtils.hasText(request.getCrmv())) {
            throw new RegistroIncompletoException("CRMV é obrigatório para o tipo VET.");
        }
        garantirLoginLivre(request.getEmail());
        if (veterinarioRepository.existsByCrmv(request.getCrmv())) {
            throw new CrmvDuplicadoException(request.getCrmv());
        }
        ClinicaEntity clinica = clinicaRepository.findFirstByOrderByIdAsc()
                .orElseThrow(ClinicaNaoProvisionadaException::new);

        VeterinarioEntity veterinario = new VeterinarioEntity();
        veterinario.setNome(request.getNome());
        veterinario.setCrmv(request.getCrmv());
        veterinario.setEmail(request.getEmail());
        veterinario.setAtivo(true);
        veterinario.setClinica(clinica);
        veterinarioRepository.save(veterinario);

        UsuarioEntity usuario = novoUsuario(request, PerfilUsuario.VET);
        usuario.setVeterinarioId(veterinario.getId());
        usuarioRepository.save(usuario);

        return LoginResponse.from(
                usuario, tokenService.emitir(usuario, clinica.getId()), veterinario.getNome());
    }

    private UsuarioEntity novoUsuario(RegistroRequest request, PerfilUsuario perfil) {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setLogin(request.getEmail());
        usuario.setSenhaHash(passwordEncoder.encode(request.getSenha()));
        usuario.setPerfil(perfil);
        usuario.setAtivo(true);
        return usuario;
    }

    // Checa inclusive o inativo: UK_USUARIO_LOGIN não distingue AT_ATIVO.
    private void garantirLoginLivre(String email) {
        if (usuarioRepository.existsByLoginIgnoreCase(email)) {
            throw new LoginDuplicadoException(email);
        }
    }
}
