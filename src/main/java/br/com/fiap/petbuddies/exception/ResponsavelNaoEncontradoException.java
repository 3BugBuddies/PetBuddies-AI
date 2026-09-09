package br.com.fiap.petbuddies.exception;

public class ResponsavelNaoEncontradoException extends RuntimeException {
    public ResponsavelNaoEncontradoException(Long id) {
        super("Responsável não encontrado para o id: " + id);
    }
}
