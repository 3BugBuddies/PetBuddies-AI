package br.com.fiap.petbuddies.exception.atendimento;

public class ProcedimentoNaoEncontradoException extends RuntimeException {
    public ProcedimentoNaoEncontradoException(Long id) {
        super("Procedimento não encontrado para o id: " + id);
    }
}
