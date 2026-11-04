package br.com.fiap.petbuddies.infrastructure.client;

import br.com.fiap.petbuddies.dto.pontuacao.LancamentoPontosResponse;
import br.com.fiap.petbuddies.dto.pontuacao.MemoriaCalculoResponse;
import br.com.fiap.petbuddies.dto.pontuacao.PontuacaoConsultaRequest;
import br.com.fiap.petbuddies.dto.pontuacao.SaldoPontosResponse;
import br.com.fiap.petbuddies.security.TokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;

// Segunda dependência do Java com o .NET: avisa a consulta realizada para pontuar o responsável,
// e lê saldo, extrato e memória de cálculo para o app, que só fala com o Java.
@Component
public class PontuacaoClient {

    private final RestClient restClient;
    private final TokenService tokenService;

    public PontuacaoClient(RestClient.Builder builder, TokenService tokenService,
                           @Value("${petnetapi.url}") String baseUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(2));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));
        this.restClient = builder.baseUrl(baseUrl).requestFactory(requestFactory).build();
        this.tokenService = tokenService;
    }

    /**
     * 201 (lançou) e 200 (já tinha lançado esta consulta) são os dois sucesso.
     *
     * @throws HttpClientErrorException o .NET recusou o corpo (4xx) — reenviar não adianta
     * @throws org.springframework.web.client.RestClientException .NET fora do ar ou erro 5xx — vale reenviar
     */
    public void pontuar(PontuacaoConsultaRequest request) {
        restClient.post()
                .uri("/api/pontuacao/consulta")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenService.emitirServico())
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    // As leituras levam o token de quem pediu, não o de serviço: o .NET lê o responsavelId
    // dele e aplica a mesma regra de dono que aplicaria ao app.

    public SaldoPontosResponse saldo(String autorizacao) {
        return restClient.get()
                .uri("/api/pontuacao/saldo")
                .header(HttpHeaders.AUTHORIZATION, autorizacao)
                .retrieve()
                .body(SaldoPontosResponse.class);
    }

    /** O .NET responde 204 sem corpo quando não há lançamento: aqui isso vira lista vazia. */
    public List<LancamentoPontosResponse> extrato(String autorizacao) {
        LancamentoPontosResponse[] extrato = restClient.get()
                .uri("/api/pontuacao/extrato")
                .header(HttpHeaders.AUTHORIZATION, autorizacao)
                .retrieve()
                .body(LancamentoPontosResponse[].class);
        return extrato == null ? List.of() : List.of(extrato);
    }

    public MemoriaCalculoResponse memoria(Long consultaId, String autorizacao) {
        return restClient.get()
                .uri("/api/pontuacao/consulta/{consultaId}/memoria", consultaId)
                .header(HttpHeaders.AUTHORIZATION, autorizacao)
                .retrieve()
                .body(MemoriaCalculoResponse.class);
    }
}
