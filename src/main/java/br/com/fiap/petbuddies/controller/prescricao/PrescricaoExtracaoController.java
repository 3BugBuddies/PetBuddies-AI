package br.com.fiap.petbuddies.controller.prescricao;

import br.com.fiap.petbuddies.dto.prescricao.NarrativaPrescricaoRequest;
import br.com.fiap.petbuddies.dto.prescricao.RascunhoPrescricaoResponse;
import br.com.fiap.petbuddies.service.prescricao.PrescricaoExtracaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Sem envelope HATEOAS — o rascunho nao e recurso, nao e persistido e nao tem self; nasce da narrativa e morre se o vet nao confirmar.
@RestController
@RequestMapping("/api/prescricao")
@Tag(name = "ia — prescrição narrada", description = "Transcreve a narrativa do veterinário para um rascunho de prescrição. Perfil VET.")
public class PrescricaoExtracaoController {

    private final PrescricaoExtracaoService service;

    public PrescricaoExtracaoController(PrescricaoExtracaoService service) {
        this.service = service;
    }

    @PostMapping("/rascunho")
    @Operation(
        summary = "Gera o rascunho de uma prescrição a partir da narrativa do veterinário",
        description = "A IA transcreve — nunca decide. O rascunho tem a forma de PrescricaoRequest "
            + "mais uma lista de regras propostas (RegraPropostaResponse, com o rótulo da condição), para a tela "
            + "pré-preencher o formulário; ele não é persistido, e só vira prescrição de verdade quando o vet "
            + "confirma. A clínica e o veterinário vêm do usuário autenticado; o registro de atendimento é "
            + "opcional. Campo que a narrativa não mencionou volta nulo; condição fora do catálogo da clínica "
            + "volta em condicaoNova (rótulo e tipo) para a vet criá-la; modelo indisponível devolve rascunho "
            + "vazio com o motivo, nunca um erro."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Rascunho gerado — completo, parcial ou degradado"),
        @ApiResponse(responseCode = "400", description = "Narrativa ou animal ausente"),
        @ApiResponse(responseCode = "403", description = "Usuário sem vínculo com veterinário"),
        @ApiResponse(responseCode = "404", description = "Animal ou registro de atendimento não encontrado")
    })
    public RascunhoPrescricaoResponse gerarRascunho(
            @RequestBody @Valid NarrativaPrescricaoRequest request, Authentication autenticacao) {
        return service.extrair(request, Long.valueOf(autenticacao.getName()));
    }
}
