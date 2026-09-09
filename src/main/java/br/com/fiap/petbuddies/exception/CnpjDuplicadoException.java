package br.com.fiap.petbuddies.exception;

public class CnpjDuplicadoException extends RuntimeException {
    public CnpjDuplicadoException(String cnpj) {
        super("Já existe uma clínica cadastrada com o CNPJ: " + cnpj);
    }
}
