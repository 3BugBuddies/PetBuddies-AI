package br.com.fiap.petbuddies.infrastructure.ia;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * Extração estruturada a partir de narrativa em texto livre.
 *
 * <p>O prompt é de quem chama — cada domínio tem o seu. O que vive aqui é a
 * chamada e a garantia de que falha de modelo nunca vira erro para o usuário.</p>
 */
@Component
public class ExtratorIA {

    private static final Logger log = LoggerFactory.getLogger(ExtratorIA.class);

    private final ChatClient chatClient;

    public ExtratorIA(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * @return o resultado, ou vazio se o modelo falhou ou devolveu corpo nulo.
     *         Nunca lança: quem chama decide o que é degradar no seu domínio.
     */
    public <T> Optional<T> extrair(String system, String narrativa, Class<T> tipo, String tag) {
        try {
            T resultado = chatClient.prompt()
                    .system(system)
                    .user(narrativa)
                    .call()
                    .entity(tipo);
            if (resultado == null) {
                log.warn("[{}] modelo devolveu corpo vazio.", tag);
                return Optional.empty();
            }
            log.debug("[{}] resposta do modelo: {}", tag, resultado);
            return Optional.of(resultado);
        } catch (Exception e) {
            log.warn("[{}] falha ao consultar o modelo: {}", tag, e.getMessage());
            return Optional.empty();
        }
    }
}
