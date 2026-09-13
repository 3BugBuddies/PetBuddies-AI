package br.com.fiap.petbuddies.exception.atendimento;

import br.com.fiap.petbuddies.exception.RecursoNaoEncontradoException;

public class JanelaAtendimentoNaoEncontradaException extends RecursoNaoEncontradoException {
    public JanelaAtendimentoNaoEncontradaException(Long id) {
        super("JANELA_ATENDIMENTO_NAO_ENCONTRADA", "Janela de atendimento não encontrada para o id: " + id);
    }
}
