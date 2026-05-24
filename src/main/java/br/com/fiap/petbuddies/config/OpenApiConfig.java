package br.com.fiap.petbuddies.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springdoc.core.customizers.OpenApiCustomizer;
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
    public OpenApiCustomizer tagOrderCustomizer() {
        return openApi -> openApi.setTags(java.util.List.of(
                new io.swagger.v3.oas.models.tags.Tag()
                        .name("catalogo — protocolos")
                        .description("CRUD e buscas customizadas de protocolos de cuidado"),
                new io.swagger.v3.oas.models.tags.Tag()
                        .name("catalogo — eventos de protocolo")
                        .description("CRUD de eventos vinculados a protocolos de cuidado"),
                new io.swagger.v3.oas.models.tags.Tag()
                        .name("motor — planos")
                        .description("Instanciação e consulta de planos de cuidado preventivo e pós-cirúrgico"),
                new io.swagger.v3.oas.models.tags.Tag()
                        .name("motor — scores")
                        .description("Cálculo e histórico de scores de risco por animal"),
                new io.swagger.v3.oas.models.tags.Tag()
                        .name("bot — simulação")
                        .description("Simulação de mensagens WhatsApp para testes sem Evolution API"),
                new io.swagger.v3.oas.models.tags.Tag()
                        .name("bot — webhook")
                        .description("Recebe eventos da Evolution API (WhatsApp gateway)")
        ));
    }
}
