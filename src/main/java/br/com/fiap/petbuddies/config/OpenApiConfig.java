package br.com.fiap.petbuddies.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
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
}
