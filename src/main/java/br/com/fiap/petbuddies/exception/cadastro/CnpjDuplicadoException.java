package br.com.fiap.petbuddies.exception.cadastro;

public class CnpjDuplicadoException extends RuntimeException {
    public CnpjDuplicadoException(String cnpj) {
        super("Já existe uma clínica cadastrada com o CNPJ: " + cnpj);
    }
}
