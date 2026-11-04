package br.com.fiap.petbuddies.controller.pontuacao;

import br.com.fiap.petbuddies.assembler.PontuacaoModelAssembler;
import br.com.fiap.petbuddies.dto.pontuacao.LancamentoPontosResponse;
import br.com.fiap.petbuddies.dto.pontuacao.MemoriaCalculoResponse;
import br.com.fiap.petbuddies.dto.pontuacao.SaldoPontosResponse;
import br.com.fiap.petbuddies.service.pontuacao.PontuacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

// Os pontos moram no PetBuddies-API (.NET); o app pede aqui, e o Java busca lá com o token de quem pediu.
@RestController
@RequestMapping("/api/pontuacao")
@Tag(name = "cuidado — pontuação", description = "Pontos que o tutor ganha a cada consulta realizada")
public class PontuacaoController {

    private final PontuacaoService pontuacaoService;
    private final PontuacaoModelAssembler assembler;

    public PontuacaoController(PontuacaoService pontuacaoService, PontuacaoModelAssembler assembler) {
        this.pontuacaoService = pontuacaoService;
        this.assembler = assembler;
    }

    @GetMapping("/saldo")
    @Operation(
        summary = "Saldo de pontos do tutor autenticado",
        description = "Soma dos pontos de todas as consultas do responsável do token. Só TUTOR."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Saldo do tutor"),
        @ApiResponse(responseCode = "403", description = "Token que não é de tutor"),
        @ApiResponse(responseCode = "503", description = "Serviço de pontuação (.NET) indisponível")
    })
    public EntityModel<SaldoPontosResponse> saldo(
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String autorizacao) {
        return assembler.toModel(pontuacaoService.saldo(autorizacao));
    }

    @GetMapping("/extrato")
    @Operation(
        summary = "Extrato de pontos do tutor autenticado",
        description = "Um lançamento por consulta realizada, da mais recente para a mais antiga. Só TUTOR."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lançamentos do tutor (lista vazia se ainda não pontuou)"),
        @ApiResponse(responseCode = "403", description = "Token que não é de tutor"),
        @ApiResponse(responseCode = "503", description = "Serviço de pontuação (.NET) indisponível")
    })
    public CollectionModel<EntityModel<LancamentoPontosResponse>> extrato(
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String autorizacao) {
        return assembler.toCollectionModel(pontuacaoService.extrato(autorizacao));
    }

    @GetMapping("/consulta/{consultaId}/memoria")
    @Operation(
        summary = "Como os pontos de uma consulta foram calculados",
        description = "O que entrou no cálculo (doses, check-ins, evolução), as regras vigentes usadas e quanto "
            + "cada critério rendeu. O veterinário vê qualquer consulta; o tutor, só as dele."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Memória de cálculo"),
        @ApiResponse(responseCode = "403", description = "Consulta de outro tutor"),
        @ApiResponse(responseCode = "404", description = "Consulta sem memória de cálculo"),
        @ApiResponse(responseCode = "503", description = "Serviço de pontuação (.NET) ou o MongoDB dele indisponível")
    })
    public EntityModel<MemoriaCalculoResponse> memoria(
            @PathVariable Long consultaId,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String autorizacao) {
        return assembler.toModel(pontuacaoService.memoria(consultaId, autorizacao));
    }
}
