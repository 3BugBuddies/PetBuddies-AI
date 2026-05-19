package br.com.fiap.petbuddies.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class PetNetApiClientConfig {

    @Bean
    public RestClient petNetApiRestClient(@Value("${petnetapi.url}") String petNetApiUrl) {
        return RestClient.builder()
                .baseUrl(petNetApiUrl)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }
}
