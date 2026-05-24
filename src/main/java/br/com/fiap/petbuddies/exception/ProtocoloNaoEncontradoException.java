package br.com.fiap.petbuddies.exception;

public class ProtocoloNaoEncontradoException extends RuntimeException {
    public ProtocoloNaoEncontradoException(Long id) {
        super("Protocolo não encontrado para o id: " + id);
    }
}
