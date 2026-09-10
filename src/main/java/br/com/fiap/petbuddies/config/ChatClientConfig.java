package br.com.fiap.petbuddies.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        // temperature=0 tambem esta em application.properties; aqui e o valor que
        // vale, porque sem ele a confianca da extracao sai 1.0 em tudo.
        return builder
                .defaultOptions(OpenAiChatOptions.builder().temperature(0.0).build())
                .build();
    }
}
