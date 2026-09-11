package br.com.fiap.petbuddies.exception.cadastro;

public class LoginDuplicadoException extends RuntimeException {
    public LoginDuplicadoException(String login) {
        super("Já existe uma conta com o e-mail: " + login);
    }
}
