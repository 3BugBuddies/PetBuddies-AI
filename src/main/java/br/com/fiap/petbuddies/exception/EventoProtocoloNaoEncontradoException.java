package br.com.fiap.petbuddies.exception;

public class EventoProtocoloNaoEncontradoException extends RuntimeException {
    public EventoProtocoloNaoEncontradoException(Long id) {
        super("Evento de protocolo não encontrado para o id: " + id);
    }
}
