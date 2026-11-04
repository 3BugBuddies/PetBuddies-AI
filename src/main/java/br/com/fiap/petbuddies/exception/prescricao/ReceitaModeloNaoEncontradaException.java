package br.com.fiap.petbuddies.exception.prescricao;

import br.com.fiap.petbuddies.exception.RecursoNaoEncontradoException;

public class ReceitaModeloNaoEncontradaException extends RecursoNaoEncontradoException {
    public ReceitaModeloNaoEncontradaException(Long id) {
        super("RECEITA_MODELO_NAO_ENCONTRADA", "Receita-modelo não encontrada para o id: " + id);
    }
}
