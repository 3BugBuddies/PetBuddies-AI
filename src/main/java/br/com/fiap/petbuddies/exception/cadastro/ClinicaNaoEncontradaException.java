package br.com.fiap.petbuddies.exception.cadastro;

public class ClinicaNaoEncontradaException extends RuntimeException {

    public ClinicaNaoEncontradaException() {
        super("Nenhuma clínica cadastrada.");
    }

    public ClinicaNaoEncontradaException(Long id) {
        super("Clínica não encontrada para o id: " + id);
    }

    public ClinicaNaoEncontradaException(String cnpj) {
        super("Clínica não encontrada para o CNPJ: " + cnpj);
    }
}
