package br.com.fiap.petbuddies.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi botGroup() {
        return GroupedOpenApi.builder()
                .group("bot")
                .pathsToMatch("/webhook/**", "/simulate-message")
                .build();
    }

    @Bean
    public GroupedOpenApi motorGroup() {
        return GroupedOpenApi.builder()
                .group("motor")
                .pathsToMatch("/api/motor/**")
                .build();
    }
}
