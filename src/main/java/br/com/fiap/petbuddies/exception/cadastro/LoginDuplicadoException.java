package br.com.fiap.petbuddies.exception.cadastro;

import br.com.fiap.petbuddies.exception.ConflitoException;

public class LoginDuplicadoException extends ConflitoException {
    public LoginDuplicadoException(String login) {
        super("LOGIN_DUPLICADO", "Já existe uma conta com o e-mail: " + login);
    }
}
