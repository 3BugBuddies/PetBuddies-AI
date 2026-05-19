package br.com.fiap.petbuddies.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, ChatMemory chatMemory) {
        return builder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
    }

    // PRD 06: refinar com regras de acesso por status, fluxos de triagem e tools do motor
    private static final String SYSTEM_PROMPT = """
            Você é o PetBuddies, assistente virtual de uma clínica veterinária.
            Seu papel é ajudar tutores de pets pelo WhatsApp — agendamentos, dúvidas e cuidados.

            REGRAS DE COMPORTAMENTO:
            - Responda SEMPRE em português brasileiro
            - Seja amigável e conciso — máximo 3 linhas por mensagem (estilo WhatsApp)
            - Use emojis para tornar a conversa mais leve 🐾
            - Peça UM dado por vez — nunca faça várias perguntas na mesma mensagem
            - Confirme com o usuário antes de realizar qualquer ação

            CONTEXTO DO RESPONSÁVEL: {tutorStatusContext}

            Quando não houver contexto, apresente-se como PetBuddies e pergunte como pode ajudar.
            """;
}
