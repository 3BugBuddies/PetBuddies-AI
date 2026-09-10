package br.com.fiap.petbuddies.controller;

import br.com.fiap.petbuddies.dto.prescricao.NarrativaPrescricaoRequest;
import br.com.fiap.petbuddies.dto.prescricao.RascunhoPrescricaoResponse;
import br.com.fiap.petbuddies.service.prescricao.ExtracaoPrescricaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A prescrição narrada (J22): o veterinário fala, a IA devolve um rascunho.
 * Sem envelope HATEOAS — o rascunho não é recurso, não é persistido e não tem
 * {@code self}; ele nasce da narrativa e morre se o vet não confirmar.
 */
@RestController
@RequestMapping("/api/prescricoes")
@Tag(name = "ia — prescrição narrada", description = "Transcreve a narrativa do veterinário para um rascunho de prescrição. Perfil VET.")
public class ExtracaoPrescricaoController {

    private final ExtracaoPrescricaoService service;

    public ExtracaoPrescricaoController(ExtracaoPrescricaoService service) {
        this.service = service;
    }

    @PostMapping("/rascunho")
    @Operation(
        summary = "Gera o rascunho de uma prescrição a partir da narrativa do veterinário",
        description = "A IA transcreve — nunca decide. O rascunho tem a forma exata de PrescricaoRequest "
            + "mais uma lista de RegraPrescricaoRequest, para a tela pré-preencher o formulário; ele não é "
            + "persistido, e só vira prescrição de verdade quando o vet confirma via POST /api/prescricoes "
            + "(e, para cada regra, POST /api/regras-prescricao com o prescricaoId devolvido). Campo que a "
            + "narrativa não mencionou volta nulo; condição fora do catálogo desta clínica é descartada e "
            + "explicada em condicoesDescartadas; modelo indisponível devolve rascunho vazio com o motivo, "
            + "nunca um erro."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Rascunho gerado — completo, parcial ou degradado"),
        @ApiResponse(responseCode = "400", description = "Narrativa ausente ou registro de atendimento não informado"),
        @ApiResponse(responseCode = "404", description = "Registro de atendimento não encontrado")
    })
    public RascunhoPrescricaoResponse gerarRascunho(@RequestBody @Valid NarrativaPrescricaoRequest request) {
        return service.extrair(request);
    }
}
