package br.com.fiap.petbuddies.exception.cuidado;

import br.com.fiap.petbuddies.exception.RecursoNaoEncontradoException;

public class ItemPlanoCuidadoNaoEncontradoException extends RecursoNaoEncontradoException {
    public ItemPlanoCuidadoNaoEncontradoException(Long id) {
        super("ITEM_PLANO_CUIDADO_NAO_ENCONTRADO", "Item de plano de cuidado não encontrado para o id: " + id);
    }
}
