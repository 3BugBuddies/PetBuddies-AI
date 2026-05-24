package br.com.fiap.petbuddies.service.bot;

import br.com.fiap.petbuddies.domain.enums.Intencao;
import br.com.fiap.petbuddies.dto.bot.IntentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ClassificadorService {

    private static final Logger log = LoggerFactory.getLogger(ClassificadorService.class);

    private static final String PROMPT = """
            Você é um classificador de intenções de um assistente veterinário via WhatsApp.
            Classifique a mensagem do usuário em UMA das intenções abaixo:
            - CADASTRO: tutor quer se cadastrar ou cadastrar um novo animal (ex: "quero cadastrar minha cachorra", "adicionar outro animal")
            - AGENDAMENTO: quer marcar, cancelar ou consultar agendamentos
            - CONSULTA_PLANO: pergunta sobre plano de cuidados, vacinas, próximos eventos, score de risco
            - TRIAGEM: descreve sintoma, comportamento anormal ou pergunta sobre urgência do animal
            - GERAL: saudação pura, dúvida genérica sobre cuidados, ou qualquer outra coisa

            PRIORIDADE: se a mensagem mistura nome de animal com sintoma (ex: "é o Apollo, meu gato está lacrimejando"),
            classifique como TRIAGEM, não como CADASTRO. Sintoma sempre tem prioridade sobre cadastro.

            REGRA CRÍTICA: Respostas curtas (1-3 palavras), valores isolados como "Sim", "Não", "Macho", "Fêmea", \
            "Pequeno", "Grande", datas (ex: "06/04/2021") ou números soltos são quase sempre continuação de um \
            formulário ativo. Se a mensagem parece uma primeira mensagem sem contexto ("sim", "não", "pequeno", "macho"), \
            classifique como GERAL. Nunca invente CADASTRO sem uma intenção de cadastro explícita.

            Responda APENAS com JSON válido, sem explicação: {"intencao":"INTENT","confianca":0.0}
            O valor de confianca deve estar entre 0.0 e 1.0.
            """;

    private final ChatClient chatClient;

    public ClassificadorService(ChatClient.Builder builder) {
        // flash-lite para classificação: 15 RPM vs 10 RPM do flash, poupa cota para a conversa principal
        this.chatClient = builder
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("gemini-2.5-flash-lite")
                        .build())
                .build();
    }

    public IntentResult classificar(String mensagem, String telefone) {
        try {
            IntentResult result = chatClient.prompt()
                    .system(PROMPT)
                    .user(mensagem)
                    .call()
                    .entity(IntentResult.class);

            if (result == null || result.getIntencao() == null) {
                return fallback();
            }

            log.debug("[INTENT] {} | confianca={} | tel={}", result.getIntencao(), result.getConfianca(), telefone);
            return result;
        } catch (Exception e) {
            log.warn("[INTENT] falha na classificação para tel={}: {}", telefone, e.getMessage());
            return fallback();
        }
    }

    public Optional<Intencao> classificarLocalmente(String mensagem) {
        String texto = RespostaParserService.normalizar(mensagem);

        if (contemAlgum(texto, "marcar consulta", "agendar", "horario", "horário",
                "cancelar consulta", "consulta para", "consulta pro", "consulta pra",
                "minhas consultas", "minha consulta", "proxima consulta", "próxima consulta",
                "tenho consulta", "consulta marcada", "consultas marcadas", "ver consulta")) {
            return Optional.of(Intencao.AGENDAMENTO);
        }
        if (contemAlgum(texto, "vomit", "diarre", "febre", "apatia", "coceira", "tosse",
                "toss", "convuls", "sangue", "dor", "respirar", "lacrimej", "olho",
                "atropel", "intoxic")) {
            return Optional.of(Intencao.TRIAGEM);
        }
        if (contemAlgum(texto, "plano", "vacina", "vermif", "cuidado pendente",
                "proximo cuidado", "proximo cuidado", "risco", "score")) {
            return Optional.of(Intencao.CONSULTA_PLANO);
        }
        if (contemAlgum(texto, "cadastrar", "cadastro", "registrar", "novo pet",
                "outro animal", "adicionar animal")) {
            return Optional.of(Intencao.CADASTRO);
        }
        return Optional.empty();
    }

    public boolean respostaCurtaSemFluxo(String mensagem) {
        String texto = RespostaParserService.normalizar(mensagem);
        return texto.matches("^(sim|s|nao|n|macho|femea|mini|pequeno|medio|grande|gigante|castrado|castrada)$")
                || texto.matches("^\\d{1,2}/\\d{1,2}/\\d{2,4}$")
                || texto.matches("^\\d+$");
    }

    public boolean isAfirmacaoSimples(String mensagem) {
        String texto = RespostaParserService.normalizar(mensagem);
        return texto.matches("^(sim|s|quero|pode|pode sim|vamos|claro|ok|por favor)$");
    }

    private static boolean contemAlgum(String texto, String... termos) {
        for (String termo : termos) {
            if (texto.contains(RespostaParserService.normalizar(termo))) return true;
        }
        return false;
    }

    private IntentResult fallback() {
        log.debug("[INTENT] GERAL (fallback)");
        return new IntentResult(Intencao.GERAL, 0.0);
    }
}
