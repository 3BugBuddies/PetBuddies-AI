package br.com.fiap.petbuddies.service;

import br.com.fiap.petbuddies.client.PetNetApiClient;
import br.com.fiap.petbuddies.dto.bot.ConversationContext;
import br.com.fiap.petbuddies.dto.bot.IntentResult;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    private final ChatClient chatClient;
    private final ClassificadorService classificador;
    private final PromptFactory promptFactory;
    private final ToolsFactory toolsFactory;
    private final PetNetApiClient petNetApiClient;

    public ChatService(ChatClient chatClient, ClassificadorService classificador,
                       PromptFactory promptFactory, ToolsFactory toolsFactory,
                       PetNetApiClient petNetApiClient) {
        this.chatClient = chatClient;
        this.classificador = classificador;
        this.promptFactory = promptFactory;
        this.toolsFactory = toolsFactory;
        this.petNetApiClient = petNetApiClient;
    }

    public String responder(String telefone, String mensagem) {
        try {
            IntentResult intent = classificador.classificar(mensagem, telefone);
            ConversationContext ctx = montarContexto(telefone, intent);
            String prompt = promptFactory.build(intent.getIntencao(), ctx);
            List<Object> tools = toolsFactory.get(intent.getIntencao());

            var builder = chatClient.prompt()
                    .system(prompt)
                    .user(mensagem)
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, telefone));

            if (!tools.isEmpty()) {
                builder = builder.tools(tools.toArray());
            }

            return builder.call().content();
        } catch (Exception e) {
            return "Desculpe, tive um problema ao processar sua mensagem. Pode tentar novamente? 🙏";
        }
    }

    private ConversationContext montarContexto(String telefone, IntentResult intent) {
        ConversationContext ctx = new ConversationContext();
        ctx.setTelefone(telefone);
        ctx.setIntencaoAtual(intent.getIntencao());
        try {
            petNetApiClient.buscarResponsavelPorTelefone(telefone).ifPresent(r -> {
                ctx.setResponsavelIdentificado(true);
                ctx.setResponsavelId(r.getId());
            });
        } catch (Exception ignored) {
            // .NET indisponível — contexto fica sem responsável identificado
        }
        return ctx;
    }
}
