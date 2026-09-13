package br.com.fiap.petbuddies.exception.cadastro;

import br.com.fiap.petbuddies.exception.ConflitoException;

public class EmailResponsavelDuplicadoException extends ConflitoException {
    public EmailResponsavelDuplicadoException(String email) {
        super("EMAIL_DUPLICADO", "Já existe um tutor cadastrado com o e-mail: " + email);
    }
}
