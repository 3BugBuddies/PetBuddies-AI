package br.com.fiap.petbuddies.exception.atendimento;

import br.com.fiap.petbuddies.exception.RecursoNaoEncontradoException;

public class CondicaoClinicaNaoEncontradaException extends RecursoNaoEncontradoException {

    private static final String CODIGO = "CONDICAO_CLINICA_NAO_ENCONTRADA";

    public CondicaoClinicaNaoEncontradaException(Long id) {
        super(CODIGO, "Condição clínica não encontrada para o id: " + id);
    }

    public CondicaoClinicaNaoEncontradaException(Long clinicaId, String codigo) {
        super(CODIGO, "Condição clínica não encontrada para o código " + codigo + " na clínica " + clinicaId);
    }
}
