package br.com.fiap.petbuddies.service.evolution;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.Map;

@Service
public class EvolutionService {

    private final RestClient restClient;
    private final String instance;

    public EvolutionService(
            @Value("${evolution.api.url}") String evolutionUrl,
            @Value("${evolution.api.key}") String apiKey,
            @Value("${evolution.api.instance}") String instance) {
        this.instance = instance;
        this.restClient = RestClient.builder()
                .baseUrl(evolutionUrl)
                .defaultHeader("apikey", apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public void enviarMensagem(String remoteJid, String texto) {
        restClient.post()
                .uri("/message/sendText/" + instance)
                .body(Map.of("number", remoteJid, "text", texto))
                .retrieve()
                .toBodilessEntity();
    }
}
