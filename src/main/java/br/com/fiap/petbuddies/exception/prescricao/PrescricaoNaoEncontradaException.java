package br.com.fiap.petbuddies.exception.prescricao;

import br.com.fiap.petbuddies.exception.RecursoNaoEncontradoException;

public class PrescricaoNaoEncontradaException extends RecursoNaoEncontradoException {
    public PrescricaoNaoEncontradaException(Long id) {
        super("PRESCRICAO_NAO_ENCONTRADA", "Prescrição não encontrada para o id: " + id);
    }
}
