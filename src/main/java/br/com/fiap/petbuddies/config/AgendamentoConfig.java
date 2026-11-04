package br.com.fiap.petbuddies.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

// Envio da pontuação fora da requisição do fechamento (@Async) e reenvio dos pendentes (@Scheduled).
@Configuration
@EnableAsync
@EnableScheduling
public class AgendamentoConfig {
}
