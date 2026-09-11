package br.com.fiap.petbuddies.exception.cadastro;

public class TelefoneResponsavelDuplicadoException extends RuntimeException {
    public TelefoneResponsavelDuplicadoException(String telefone) {
        super("Já existe um tutor cadastrado com o telefone: " + telefone);
    }
}
