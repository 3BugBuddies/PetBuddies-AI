package br.com.fiap.petbuddies.exception.cadastro;

import br.com.fiap.petbuddies.exception.RecursoNaoEncontradoException;

public class ClinicaNaoEncontradaException extends RecursoNaoEncontradoException {

    private static final String CODIGO = "CLINICA_NAO_ENCONTRADA";

    public ClinicaNaoEncontradaException() {
        super(CODIGO, "Nenhuma clínica cadastrada.");
    }

    public ClinicaNaoEncontradaException(Long id) {
        super(CODIGO, "Clínica não encontrada para o id: " + id);
    }

    public ClinicaNaoEncontradaException(String cnpj) {
        super(CODIGO, "Clínica não encontrada para o CNPJ: " + cnpj);
    }
}
