package br.com.fiap.petbuddies.exception;

public class RegraProtocoloNaoEncontradoException extends RuntimeException {
    public RegraProtocoloNaoEncontradoException(Long id) {
        super("Evento de protocolo não encontrado para o id: " + id);
    }
}
