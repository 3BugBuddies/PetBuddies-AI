package br.com.fiap.petbuddies.service;

import br.com.fiap.petbuddies.domain.enums.Intencao;
import br.com.fiap.petbuddies.dto.bot.IntentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ClassificadorService {

    private static final Logger log = LoggerFactory.getLogger(ClassificadorService.class);

    private static final String PROMPT = """
            Você é um classificador de intenções de um assistente veterinário via WhatsApp.
            Classifique a mensagem do usuário em UMA das intenções abaixo:
            - CADASTRO: tutor quer se cadastrar ou cadastrar um animal
            - AGENDAMENTO: quer marcar, cancelar ou consultar agendamentos
            - CONSULTA_PLANO: pergunta sobre plano de cuidados, vacinas, score de risco
            - TRIAGEM: descreve sintoma ou pergunta sobre urgência do animal
            - GERAL: saudação, dúvida genérica ou qualquer outra coisa
            Responda APENAS com JSON válido, sem explicação: {"intencao":"INTENT","confianca":0.0}
            O valor de confianca deve estar entre 0.0 e 1.0.
            """;

    private final ChatClient chatClient;

    public ClassificadorService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
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

    private IntentResult fallback() {
        log.debug("[INTENT] GERAL (fallback)");
        return new IntentResult(Intencao.GERAL, 0.0);
    }
}
