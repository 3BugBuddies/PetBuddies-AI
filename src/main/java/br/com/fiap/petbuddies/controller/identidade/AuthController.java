package br.com.fiap.petbuddies.controller.identidade;

import br.com.fiap.petbuddies.dto.identidade.LoginRequest;
import br.com.fiap.petbuddies.dto.identidade.LoginResponse;
import br.com.fiap.petbuddies.dto.identidade.RegistroRequest;
import br.com.fiap.petbuddies.dto.identidade.TrocaSenhaRequest;
import br.com.fiap.petbuddies.service.identidade.AutenticacaoService;
import br.com.fiap.petbuddies.service.identidade.RegistroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "autenticação", description = "Login dos dois perfis e emissão do token")
public class AuthController {

    private final AutenticacaoService autenticacaoService;
    private final RegistroService registroService;

    public AuthController(AutenticacaoService autenticacaoService, RegistroService registroService) {
        this.autenticacaoService = autenticacaoService;
        this.registroService = registroService;
    }

    @PostMapping("/login")
    @Operation(
        summary = "Autenticar e receber o token",
        description = "Único login dos dois perfis. O token carrega perfil e vínculo, e serve nas duas APIs. "
            + "A resposta não vem em envelope HATEOAS: um token não é recurso navegável."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Autenticado — token, perfil e vínculo"),
        @ApiResponse(responseCode = "400", description = "Corpo sem login ou sem senha"),
        @ApiResponse(responseCode = "401",
            description = "Credenciais inválidas. Mesma mensagem para login inexistente, "
                + "senha errada e usuário inativo")
    })
    public LoginResponse login(@RequestBody @Valid LoginRequest request) {
        return autenticacaoService.autenticar(request);
    }

    @PostMapping("/registro")
    @Operation(
        summary = "Cadastrar tutor ou veterinário e já receber o token",
        description = "Rota aberta. O tipo decide o que nasce: TUTOR cria o responsável, VET cria o "
            + "veterinário na clínica provisionada. Clínica não se cadastra por esta rota. "
            + "Como o login, a resposta não vem em envelope HATEOAS."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Cadastrado — token, perfil, vínculo e nome"),
        @ApiResponse(responseCode = "400", description = "Corpo inválido, ou sem o campo que o tipo exige"),
        @ApiResponse(responseCode = "409", description = "E-mail, telefone ou CRMV já cadastrados"),
        @ApiResponse(responseCode = "422", description = "Nenhuma clínica provisionada")
    })
    public ResponseEntity<LoginResponse> registrar(@RequestBody @Valid RegistroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registroService.registrar(request));
    }

    @PostMapping("/senha")
    @Operation(
        summary = "Trocar a própria senha",
        description = "Exige token. Troca a senha do usuário do token, confirmando a atual — "
            + "não existe troca de senha de terceiro. Tokens já emitidos seguem válidos até expirar."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Senha trocada"),
        @ApiResponse(responseCode = "400", description = "Corpo sem senha atual ou nova"),
        @ApiResponse(responseCode = "401", description = "Senha atual incorreta")
    })
    public ResponseEntity<Void> trocarSenha(
            Authentication autenticacao, @RequestBody @Valid TrocaSenhaRequest request) {
        registroService.trocarSenha(Long.valueOf(autenticacao.getName()), request);
        return ResponseEntity.noContent().build();
    }
}
