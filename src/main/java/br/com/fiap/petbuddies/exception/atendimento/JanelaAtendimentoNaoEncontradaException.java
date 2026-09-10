package br.com.fiap.petbuddies.exception.atendimento;

public class JanelaAtendimentoNaoEncontradaException extends RuntimeException {
    public JanelaAtendimentoNaoEncontradaException(Long id) {
        super("Janela de atendimento não encontrada para o id: " + id);
    }
}
