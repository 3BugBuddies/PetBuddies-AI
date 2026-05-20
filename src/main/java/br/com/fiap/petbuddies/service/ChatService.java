package br.com.fiap.petbuddies.service;

import br.com.fiap.petbuddies.client.PetNetApiClient;
import br.com.fiap.petbuddies.domain.enums.Intencao;
import br.com.fiap.petbuddies.dto.bot.ConversationContext;
import br.com.fiap.petbuddies.dto.bot.IntentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    // fluxo ativo por telefone
    private final Map<String, Intencao> flowAtivo = new ConcurrentHashMap<>();

    // cada fluxo tem seu próprio conversationId — isola o histórico JDBC entre fluxos
    private final Map<String, String> conversationIds = new ConcurrentHashMap<>();

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
            if (isReset(mensagem)) {
                flowAtivo.remove(telefone);
                conversationIds.remove(telefone);
                return "Certo, zerei o contexto desta conversa. Como posso ajudar agora?";
            }

            Intencao intencao = resolverIntencao(telefone, mensagem);

            String convId = conversationIds.computeIfAbsent(telefone,
                    t -> t + "_" + System.currentTimeMillis());

            ConversationContext ctx = montarContexto(telefone, intencao);
            String prompt = promptFactory.build(intencao, ctx);
            List<Object> tools = toolsFactory.get(intencao);

            log.debug("[CHAT] tel={} intencao={} convId={}", telefone, intencao, convId);

            var builder = chatClient.prompt()
                    .system(prompt)
                    .user(mensagem)
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, convId));

            if (!tools.isEmpty()) {
                builder = builder.tools(tools.toArray());
            }

            return builder.call().content();
        } catch (Exception e) {
            log.error("[CHAT] erro tel={}: {} — {}", telefone, e.getClass().getSimpleName(), e.getMessage(), e);
            return mensagemDeErro(e);
        }
    }

    /**
     * Resolve a intenção mantendo o fluxo ativo para mensagens curtas ou ambíguas.
     * Quando o fluxo muda genuinamente, gera um novo conversationId — o histórico
     * JDBC do fluxo anterior fica no banco mas nunca é lido no novo fluxo.
     */
    private Intencao resolverIntencao(String telefone, String mensagem) {
        Intencao ativo = flowAtivo.get(telefone);
        boolean mensagemCurta = mensagem.trim().split("\\s+").length <= 5;

        Optional<Intencao> local = classificarLocalmente(mensagem);
        if (local.isPresent()) {
            Intencao nova = local.get();
            if (ativo == null || nova != ativo) {
                conversationIds.put(telefone, telefone + "_" + System.currentTimeMillis());
                log.debug("[FLOW] tel={} {} → {} por heurística local", telefone, ativo, nova);
            }
            flowAtivo.put(telefone, nova);
            return nova;
        }

        if (ativo != null && mensagemCurta) {
            log.debug("[FLOW] tel={} manteve {} (curta, sem classificação)", telefone, ativo);
            return ativo;
        }

        IntentResult classificado = classificador.classificar(mensagem, telefone);
        Intencao nova = classificado.getIntencao();
        boolean novaIntencaoClara = nova != Intencao.GERAL && classificado.getConfianca() >= 0.75;
        boolean mudouFluxo = ativo == null || nova != ativo;

        if (novaIntencaoClara && mudouFluxo) {
            flowAtivo.put(telefone, nova);
            conversationIds.put(telefone, telefone + "_" + System.currentTimeMillis());
            log.debug("[FLOW] tel={} {} → {}, novo contexto isolado", telefone, ativo, nova);
            return nova;
        }

        if (ativo != null) {
            return ativo;
        }

        flowAtivo.put(telefone, nova);
        return nova;
    }

    private Optional<Intencao> classificarLocalmente(String mensagem) {
        String texto = normalizar(mensagem);

        if (contemAlgum(texto, "marcar consulta", "agendar", "horario", "horário",
                "cancelar consulta", "consulta para", "consulta pro", "consulta pra")) {
            return Optional.of(Intencao.AGENDAMENTO);
        }
        if (contemAlgum(texto, "plano", "vacina", "vermif", "cuidado pendente",
                "proximo cuidado", "próximo cuidado", "risco", "score")) {
            return Optional.of(Intencao.CONSULTA_PLANO);
        }
        if (contemAlgum(texto, "cadastrar", "cadastro", "registrar", "novo pet",
                "outro animal", "adicionar animal")) {
            return Optional.of(Intencao.CADASTRO);
        }
        return Optional.empty();
    }

    private boolean isReset(String mensagem) {
        String texto = normalizar(mensagem);
        return contemAlgum(texto, "comecar de novo", "começar de novo", "resetar", "zerar",
                "cancelar fluxo", "sair do fluxo");
    }

    private static boolean contemAlgum(String texto, String... termos) {
        for (String termo : termos) {
            if (texto.contains(normalizar(termo))) {
                return true;
            }
        }
        return false;
    }

    private static String normalizar(String valor) {
        if (valor == null) return "";
        String semAcento = Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcento.toLowerCase(Locale.ROOT).trim();
    }

    private ConversationContext montarContexto(String telefone, Intencao intencao) {
        ConversationContext ctx = new ConversationContext();
        ctx.setTelefone(telefone);
        ctx.setIntencaoAtual(intencao);
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

    private String mensagemDeErro(Exception e) {
        String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        Throwable causa = e.getCause() != null ? e.getCause() : e;
        String causaMsg = causa.getMessage() != null ? causa.getMessage().toLowerCase() : "";

        if (msg.contains("429") || causaMsg.contains("429")
                || msg.contains("rate limit") || msg.contains("quota")
                || msg.contains("resource_exhausted") || causaMsg.contains("too many requests")) {
            return "Estou com muitas conversas agora 😅 Aguarde alguns segundos e tente novamente!";
        }
        if (msg.contains("timeout") || causaMsg.contains("timeout")
                || msg.contains("timed out") || msg.contains("connect")) {
            return "A resposta demorou mais do que esperado 🕐 Pode tentar novamente?";
        }
        return "Desculpe, tive um problema ao processar sua mensagem. Pode tentar novamente? 🙏";
    }
}
