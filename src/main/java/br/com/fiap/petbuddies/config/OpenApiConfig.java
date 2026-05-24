package br.com.fiap.petbuddies.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
    info = @Info(
        title = "PetBuddies AI",
        version = "1.0.0",
        description = "Motor de cuidado contínuo e bot WhatsApp para tutores de pets.",
        contact = @Contact(name = "FIAP 2TDS 2026 — PetBuddies", email = "spbiel18@gmail.com")
    )
)
@Configuration
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi catalogoApi() {
        return GroupedOpenApi.builder()
                .group("catalogo")
                .pathsToMatch("/api/protocolos/**", "/api/eventos-protocolo/**")
                .build();
    }

    @Bean
    public GroupedOpenApi motorApi() {
        return GroupedOpenApi.builder()
                .group("motor")
                .pathsToMatch("/api/motor/**")
                .build();
    }

    @Bean
    public GroupedOpenApi botApi() {
        return GroupedOpenApi.builder()
                .group("bot")
                .pathsToMatch("/webhook/**", "/simulate-message/**")
                .build();
    }
}
