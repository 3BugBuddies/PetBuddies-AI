package br.com.fiap.petbuddies.exception;

public class ClinicaNaoEncontradaException extends RuntimeException {

    public ClinicaNaoEncontradaException(Long id) {
        super("Clínica não encontrada para o id: " + id);
    }

    public ClinicaNaoEncontradaException(String cnpj) {
        super("Clínica não encontrada para o CNPJ: " + cnpj);
    }
}
