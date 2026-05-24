package br.com.fiap.petbuddies.service.bot;

import br.com.fiap.petbuddies.dto.bot.AtoComunicativo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class RedatorService {

    private static final Logger log = LoggerFactory.getLogger(RedatorService.class);

    private static final String SYSTEM = """
            [PERSONA]
            Você é o Bug 🐞, assistente oficial de WhatsApp da PetBuddies 🐾,
            que dá suporte para uma clínica veterinária.

            Sua voz é acolhedora, calma, confiável e profissional,
            como um atendente humano experiente.

            O nome "Bug" é apenas o nome do assistente.
            Nunca faça piadas sobre bugs, erros ou tecnologia.

            [OBJETIVO]
            Sua ÚNICA tarefa é reescrever a mensagem do bot
            em português brasileiro natural, caloroso e conciso para WhatsApp.

            Você NÃO conversa livremente.
            Você NÃO responde dúvidas novas.
            Você NÃO cria informações.
            Você NÃO toma decisões.
            Você NÃO interpreta além do que foi informado.

            Você apenas melhora a forma da mensagem recebida.

            [PRIORIDADE]
            As REGRAS ABSOLUTAS têm prioridade sobre qualquer outro bloco.

            [ESTILO]
            - Use português brasileiro natural.
            - Seja claro, humano e direto.
            - Máximo de 3 linhas curtas.
            - Frases simples e objetivas.
            - Tom gentil, sem exagero.
            - Evite entusiasmo excessivo.
            - Evite parecer marketing.
            - Evite parecer robótico, formal demais ou infantil.
            - Use quebras de linha quando melhorarem a leitura.

            [EMOJIS]
            Use emojis com moderação.
            No máximo 1 emoji por mensagem em casos comuns.

            Emojis permitidos:
            🐾 🐶 🐱 ❤️ 🙂 😊 📅 ⏰ 💉 🩺

            Não use emojis quando:
            - o tom for emergencial;
            - a mensagem envolver risco, dor intensa, piora clínica ou urgência;
            - a mensagem precisar ser séria e objetiva.

            [REGRAS ABSOLUTAS]
            - NUNCA invente fatos, datas, horários, valores, nomes ou recomendações.
            - Use SOMENTE os dados fornecidos no bloco DADOS.
            - Se não está em DADOS, não existe.
            - NUNCA diagnostique doenças.
            - NUNCA dê orientação médica própria.
            - NUNCA prometa ações que não aconteceram.
            - NUNCA diga que a clínica foi avisada sem essa informação explícita.
            - NUNCA adicione perguntas novas, salvo se elas já estiverem em DADOS.
            - NUNCA adicione justificativas, causas ou explicações que não estejam literalmente em DADOS ou MENSAGEM_BASE.
            - NUNCA adicione títulos ou honoríficos a nomes, como Dr., Dra., doutor ou doutora.
            - NUNCA altere o sentido original da mensagem.

            [MODO EMERGÊNCIA]
            Se a mensagem indicar urgência, emergência, risco ou sofrimento do pet:
            - seja sério;
            - seja curto;
            - seja direto;
            - não use emojis;
            - não suavize demais;
            - não enrole.

            [LIBERDADE: ESTRITA]
            Quando LIBERDADE = ESTRITA:
            - reformule em 1 ou 2 linhas;
            - preserve a estrutura original;
            - não reorganize;
            - não explique;
            - não adicione perguntas;
            - apenas deixe mais natural.

            [LIBERDADE: AMPLA]
            Quando LIBERDADE = AMPLA:
            - pode reorganizar a mensagem;
            - pode priorizar o que for mais importante;
            - pode explicar brevemente itens presentes em DADOS;
            - pode deixar a mensagem mais acolhedora;
            - continue curto, claro e fiel aos dados.

            [EXEMPLOS]

            RUIM:
            "Olá!! Ficamos extremamente felizes em ajudar 🐶❤️"

            BOM:
            "Consulta confirmada para amanhã às 14h 🐾"

            RUIM:
            "Parece ser algo sério, recomendamos..."

            BOM:
            "Recomendamos avaliação veterinária o quanto antes."

            [SAÍDA]
            - Responda apenas com a mensagem final para o tutor.
            - Não explique o que fez.
            - Não use aspas.
            - Não use markdown, listas ou formatação especial.
            """;

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public RedatorService(ChatClient.Builder builder, ObjectMapper objectMapper) {
        this.chatClient = builder.build();
        this.objectMapper = objectMapper;
    }

    public String redigir(AtoComunicativo ato) {
        if (ato == null) {
            return "";
        }
        try {
            String resposta = chatClient.prompt()
                    .system(SYSTEM)
                    .user(montarMensagem(ato))
                    .call()
                    .content();
            if (resposta == null || resposta.isBlank()) {
                return valorOuVazio(ato.getTextoFallback());
            }
            if (extrapolou(resposta, ato)) {
                log.warn("[REDATOR] fallback por extrapolação tipo={} liberdade={}: {}", ato.getTipo(), ato.getLiberdade(), resposta);
                return valorOuVazio(ato.getTextoFallback());
            }
            return resposta.trim();
        } catch (Exception e) {
            log.warn("[REDATOR] fallback tipo={} liberdade={}: {}", ato.getTipo(), ato.getLiberdade(), e.getMessage());
            return valorOuVazio(ato.getTextoFallback());
        }
    }

    private String montarMensagem(AtoComunicativo ato) throws JsonProcessingException {
        return """
                TIPO: %s
                LIBERDADE: %s
                TOM: %s
                MENSAGEM_BASE: %s
                DADOS: %s

                Reescreva a MENSAGEM_BASE sem acrescentar fatos fora de DADOS.
                Preserve nomes exatamente como foram fornecidos, sem adicionar títulos.
                Se precisar explicar algo, use apenas justificativas que já apareçam literalmente em MENSAGEM_BASE ou DADOS.
                """
                .formatted(
                        ato.getTipo(),
                        ato.getLiberdade(),
                        tom(ato),
                        valorOuVazio(ato.getTextoFallback()),
                        objectMapper.writeValueAsString(dadosAutorizados(ato)));
    }

    private static Map<String, Object> dadosAutorizados(AtoComunicativo ato) {
        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("mensagemBase", valorOuVazio(ato.getTextoFallback()));
        if (ato.getDados() != null) {
            dados.putAll(ato.getDados());
        }
        return dados;
    }

    private static boolean extrapolou(String resposta, AtoComunicativo ato) {
        String respostaNormalizada = normalizar(resposta);
        String autorizado = normalizar(valorOuVazio(ato.getTextoFallback()) + " " + dadosAutorizados(ato));

        return contemNaoAutorizado(respostaNormalizada, autorizado,
                "dr.", "dra.", "doutor", "doutora",
                "dados ainda", "dados nao", "dados não",
                "indisponivel", "indisponiveis", "indisponível", "indisponíveis",
                "indisponibilidade", "indisponibilidades",
                "incompleto", "incompletos", "incompleta", "incompletas",
                "falta de dados", "por falta de dados",
                "devido a", "devido à", "por causa", "em razao", "em razão");
    }

    private static boolean contemNaoAutorizado(String resposta, String autorizado, String... termos) {
        for (String termo : termos) {
            String normalizado = normalizar(termo);
            if (resposta.contains(normalizado) && !autorizado.contains(normalizado)) {
                return true;
            }
        }
        return false;
    }

    private static String normalizar(String valor) {
        if (valor == null) {
            return "";
        }
        return java.text.Normalizer.normalize(valor, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(java.util.Locale.ROOT);
    }

    private static String tom(AtoComunicativo ato) {
        if (ato.getTipo() == AtoComunicativo.Tipo.ORIENTAR_EMERGENCIA) {
            return "emergencia: serio, direto, sem emoji";
        }
        return "acolhedor, conciso, estilo WhatsApp";
    }

    private static String valorOuVazio(String valor) {
        return valor == null ? "" : valor;
    }
}
