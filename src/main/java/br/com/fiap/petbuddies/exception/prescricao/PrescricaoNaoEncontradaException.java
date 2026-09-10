package br.com.fiap.petbuddies.exception.prescricao;

public class PrescricaoNaoEncontradaException extends RuntimeException {
    public PrescricaoNaoEncontradaException(Long id) {
        super("Prescrição não encontrada para o id: " + id);
    }
}
