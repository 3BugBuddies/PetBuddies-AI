package br.com.fiap.petbuddies.exception;

public class AnimalNaoEncontradoException extends RuntimeException {
    public AnimalNaoEncontradoException(Long id) {
        super("Animal não encontrado para o id: " + id);
    }
}
