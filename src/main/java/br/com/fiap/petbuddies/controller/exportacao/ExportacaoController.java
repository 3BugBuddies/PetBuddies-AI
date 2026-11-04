package br.com.fiap.petbuddies.controller.exportacao;

import br.com.fiap.petbuddies.service.exportacao.ExportacaoDatasetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

// Arquivo para download, não recurso navegável: sem envelope HATEOAS, como a extração do check-in.
@RestController
@RequestMapping("/api/exportacao")
@Tag(name = "banco — exportação", description = "Dataset em JSON gerado por procedimento PL/SQL, para importar no MongoDB")
public class ExportacaoController {

    private static final MediaType JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8);

    private final ExportacaoDatasetService exportacaoService;

    public ExportacaoController(ExportacaoDatasetService exportacaoService) {
        this.exportacaoService = exportacaoService;
    }

    @GetMapping(value = "/planos-cuidado", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Exporta os planos de cuidado em JSON (procedimento PKG_PB_JSON.PLANO_JSON)",
        description = "Executa no Oracle o procedimento que faz o JOIN de animal, plano e itens e devolve "
            + "um array com um documento por plano, os eventos embutidos. Sem animalId, o dataset inteiro — "
            + "o arquivo que se importa no MongoDB com mongoimport --jsonArray. Só VET."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Arquivo planos_cuidado.json"),
        @ApiResponse(responseCode = "403", description = "Token que não é de veterinário"),
        @ApiResponse(responseCode = "404", description = "Animal inexistente, ou sem plano de cuidado"),
        @ApiResponse(responseCode = "409", description = "Plano sem itens: a instanciação parou no meio")
    })
    public ResponseEntity<String> planosCuidado(
            @Parameter(description = "Id do animal; vazio exporta todos") @RequestParam(required = false) Long animalId) {
        String arquivo = animalId == null ? "planos_cuidado.json" : "planos_cuidado_animal_" + animalId + ".json";
        return ResponseEntity.ok()
                .contentType(JSON_UTF8)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(arquivo).build().toString())
                .body(exportacaoService.planosCuidado(animalId));
    }
}
