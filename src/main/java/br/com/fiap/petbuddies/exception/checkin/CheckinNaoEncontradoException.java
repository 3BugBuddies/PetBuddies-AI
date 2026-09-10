package br.com.fiap.petbuddies.exception.checkin;

public class CheckinNaoEncontradoException extends RuntimeException {
    public CheckinNaoEncontradoException(Long id) {
        super("Check-in não encontrado para o id: " + id);
    }
}
