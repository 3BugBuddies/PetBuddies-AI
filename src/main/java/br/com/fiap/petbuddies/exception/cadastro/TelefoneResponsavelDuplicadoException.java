package br.com.fiap.petbuddies.exception.cadastro;

import br.com.fiap.petbuddies.exception.ConflitoException;

public class TelefoneResponsavelDuplicadoException extends ConflitoException {
    public TelefoneResponsavelDuplicadoException(String telefone) {
        super("TELEFONE_DUPLICADO", "Já existe um tutor cadastrado com o telefone: " + telefone);
    }
}
