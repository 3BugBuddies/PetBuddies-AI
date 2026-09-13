package br.com.fiap.petbuddies.exception.checkin;

import br.com.fiap.petbuddies.exception.RecursoNaoEncontradoException;

public class CheckinNaoEncontradoException extends RecursoNaoEncontradoException {
    public CheckinNaoEncontradoException(Long id) {
        super("CHECKIN_NAO_ENCONTRADO", "Check-in não encontrado para o id: " + id);
    }
}
