package br.com.fiap.petbuddies.exception;

public class RegraPrescricaoNaoEncontradaException extends RuntimeException {
    public RegraPrescricaoNaoEncontradaException(Long id) {
        super("Regra de prescrição não encontrada para o id: " + id);
    }
}
