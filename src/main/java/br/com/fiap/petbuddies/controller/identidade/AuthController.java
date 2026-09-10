package br.com.fiap.petbuddies.controller.identidade;

import br.com.fiap.petbuddies.dto.identidade.LoginRequest;
import br.com.fiap.petbuddies.dto.identidade.LoginResponse;
import br.com.fiap.petbuddies.service.identidade.AutenticacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "autenticação", description = "Login dos dois perfis e emissão do token")
public class AuthController {

    private final AutenticacaoService autenticacaoService;

    public AuthController(AutenticacaoService autenticacaoService) {
        this.autenticacaoService = autenticacaoService;
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
}
