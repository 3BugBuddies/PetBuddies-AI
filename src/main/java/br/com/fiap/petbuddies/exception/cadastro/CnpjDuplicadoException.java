package br.com.fiap.petbuddies.exception.cadastro;

import br.com.fiap.petbuddies.exception.ConflitoException;

public class CnpjDuplicadoException extends ConflitoException {
    public CnpjDuplicadoException(String cnpj) {
        super("CNPJ_DUPLICADO", "Já existe uma clínica cadastrada com o CNPJ: " + cnpj);
    }
}
