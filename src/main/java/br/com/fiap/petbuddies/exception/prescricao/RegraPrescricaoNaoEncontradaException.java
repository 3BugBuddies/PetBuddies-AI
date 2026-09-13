package br.com.fiap.petbuddies.exception.prescricao;

import br.com.fiap.petbuddies.exception.RecursoNaoEncontradoException;

public class RegraPrescricaoNaoEncontradaException extends RecursoNaoEncontradoException {
    public RegraPrescricaoNaoEncontradaException(Long id) {
        super("REGRA_PRESCRICAO_NAO_ENCONTRADA", "Regra de prescrição não encontrada para o id: " + id);
    }
}
