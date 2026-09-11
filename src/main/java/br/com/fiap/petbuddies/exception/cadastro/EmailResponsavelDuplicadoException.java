package br.com.fiap.petbuddies.exception.cadastro;

public class EmailResponsavelDuplicadoException extends RuntimeException {
    public EmailResponsavelDuplicadoException(String email) {
        super("Já existe um tutor cadastrado com o e-mail: " + email);
    }
}
