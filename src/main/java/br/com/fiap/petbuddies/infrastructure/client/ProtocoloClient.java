package br.com.fiap.petbuddies.infrastructure.client;

import br.com.fiap.petbuddies.domain.enums.cuidado.CategoriaProtocolo;
import br.com.fiap.petbuddies.domain.enums.cadastro.Especie;
import br.com.fiap.petbuddies.security.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.List;

/**
 * Le o catalogo de protocolos do .NET (ADR s3-25) — a unica dependencia entre
 * os dois servicos. So e chamado ao criar um plano; depois de materializado, o
 * plano nao volta a consulta-lo.
 */
@Component
public class ProtocoloClient {

    private static final Logger log = LoggerFactory.getLogger(ProtocoloClient.class);

    private final RestClient restClient;
    private final TokenService tokenService;

    public ProtocoloClient(RestClient.Builder builder, TokenService tokenService,
                            @Value("${petnetapi.url}") String baseUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(2));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));
        this.restClient = builder.baseUrl(baseUrl).requestFactory(requestFactory).build();
        this.tokenService = tokenService;
    }

    /**
     * Protocolos ativos da categoria e especie. Lista vazia tanto para "nenhum
     * protocolo compativel" quanto para "catalogo fora do ar" — as duas viram a
     * mesma resposta no motor (ADR s3-25): o plano so nao nasce agora.
     */
    public List<ProtocoloCatalogoDto> buscar(CategoriaProtocolo categoria, Especie especie) {
        try {
            ProtocoloCatalogoDto[] resposta = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/protocolo")
                            .queryParam("categoria", categoria)
                            .queryParam("especie", especie)
                            .queryParam("ativo", true)
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenService.emitirServico())
                    .retrieve()
                    .body(ProtocoloCatalogoDto[].class);
            return resposta == null ? List.of() : List.of(resposta);
        } catch (RestClientException ex) {
            log.warn("Catalogo de protocolos indisponivel no .NET (categoria={}, especie={}): {}",
                    categoria, especie, ex.getMessage());
            return List.of();
        }
    }
}
