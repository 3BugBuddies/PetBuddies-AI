package br.com.fiap.petbuddies.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * O esquema de autorizacao e <b>declarado</b>, e nao exigido globalmente: o
 * login e zona aberta, e marcar toda a API como protegida documentaria o
 * contrato errado. Declarado assim, o botao de autorizar aparece no Swagger e
 * o token vale para as rotas que de fato o pedem.
 */
@OpenAPIDefinition(
    info = @Info(
        title = "PetBuddies AI",
        version = "1.0.0",
        description = "Motor de cuidado contínuo: catálogo de protocolos e planos por animal.",
        contact = @Contact(name = "FIAP 2TDS 2026 — PetBuddies", email = "spbiel18@gmail.com")
    )
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "Token emitido por POST /api/auth/login. Vale também no serviço .NET (ADR s3-20)."
)
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenApiCustomizer tagOrderCustomizer() {
        return openApi -> openApi.setTags(java.util.List.of(
                new io.swagger.v3.oas.models.tags.Tag()
                        .name("autenticação")
                        .description("Login dos dois perfis e emissão do token"),
                new io.swagger.v3.oas.models.tags.Tag()
                        .name("motor — planos")
                        .description("Instanciação e consulta de planos de cuidado preventivo e pós-cirúrgico"),
                new io.swagger.v3.oas.models.tags.Tag()
                        .name("registro — clínicas")
                        .description("CRUD de clínicas, a raiz do registro clínico"),
                new io.swagger.v3.oas.models.tags.Tag()
                        .name("registro — responsáveis")
                        .description("CRUD de tutores, o dono do animal no registro clínico"),
                new io.swagger.v3.oas.models.tags.Tag()
                        .name("registro — veterinários")
                        .description("CRUD da equipe clínica, quem assina o ato"),
                new io.swagger.v3.oas.models.tags.Tag()
                        .name("registro — animais")
                        .description("CRUD de pacientes, o animal do registro clínico"),
                new io.swagger.v3.oas.models.tags.Tag()
                        .name("registro — consultas")
                        .description("Agendamento e comparecimento do animal na clínica"),
                new io.swagger.v3.oas.models.tags.Tag()
                        .name("registro — condições clínicas")
                        .description("Catálogo de condições que o check-in avalia, por clínica"),
                new io.swagger.v3.oas.models.tags.Tag()
                        .name("registro — janelas de atendimento")
                        .description("Agenda do veterinário: slots de 30 minutos, livres ou reservados por uma consulta"),
                new io.swagger.v3.oas.models.tags.Tag()
                        .name("registro — registros de atendimento")
                        .description("O que aconteceu na consulta: anamnese, diagnóstico e tratamento"),
                new io.swagger.v3.oas.models.tags.Tag()
                        .name("registro — procedimentos")
                        .description("Vacina, exame ou cirurgia executados num atendimento"),
                new io.swagger.v3.oas.models.tags.Tag()
                        .name("registro — prescrições")
                        .description("O ato assinado pelo veterinário — imutável depois de criado"),
                new io.swagger.v3.oas.models.tags.Tag()
                        .name("registro — regras de prescrição")
                        .description("Condição → ação sobre a dose, com a condição congelada no momento da assinatura")
        ));
    }
}
