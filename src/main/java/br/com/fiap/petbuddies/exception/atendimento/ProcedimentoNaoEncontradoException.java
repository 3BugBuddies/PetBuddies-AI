package br.com.fiap.petbuddies.exception.atendimento;

import br.com.fiap.petbuddies.exception.RecursoNaoEncontradoException;

public class ProcedimentoNaoEncontradoException extends RecursoNaoEncontradoException {
    public ProcedimentoNaoEncontradoException(Long id) {
        super("PROCEDIMENTO_NAO_ENCONTRADO", "Procedimento não encontrado para o id: " + id);
    }
}
