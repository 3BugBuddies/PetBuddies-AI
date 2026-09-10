package br.com.fiap.petbuddies.exception.identidade;

/**
 * Recusa de login. <b>Uma excecao so, e uma mensagem so</b>, para os tres casos:
 * login inexistente, senha errada e usuario inativo. Distinguir os tres na
 * resposta diria a quem tenta quais logins existem.
 */
public class CredenciaisInvalidasException extends RuntimeException {

    public static final String MENSAGEM = "Login ou senha inválidos.";

    public CredenciaisInvalidasException() {
        super(MENSAGEM);
    }
}
