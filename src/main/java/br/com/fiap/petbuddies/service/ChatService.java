package br.com.fiap.petbuddies.service;

import br.com.fiap.petbuddies.client.PetNetApiClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final ChatClient chatClient;
    private final PetNetApiClient petNetApi;

    public ChatService(ChatClient chatClient, PetNetApiClient petNetApi) {
        this.chatClient = chatClient;
        this.petNetApi = petNetApi;
    }

    public String responder(String telefone, String texto) {
        String statusContext = resolverStatusContext(telefone);
        try {
            return chatClient.prompt()
                    .system(s -> s.param("tutorStatusContext", statusContext))
                    .user(texto)
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, telefone))
                    .call()
                    .content();
        } catch (Exception e) {
            return "Desculpe, tive um problema ao processar sua mensagem. Pode tentar novamente? 🙏";
        }
    }

    private String resolverStatusContext(String telefone) {
        try {
            return petNetApi.buscarResponsavelPorTelefone(telefone)
                    .map(r -> "Responsável identificado: " + r.getNome() + " | Status: " + r.getStatus())
                    .orElse("Responsável não cadastrado neste número.");
        } catch (Exception e) {
            return "Status do responsável indisponível no momento.";
        }
    }
}
