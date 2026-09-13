package br.com.fiap.petbuddies.exception.cuidado;

import br.com.fiap.petbuddies.exception.RecursoNaoEncontradoException;

public class PlanoNaoEncontradoException extends RecursoNaoEncontradoException {
    public PlanoNaoEncontradoException(Long animalId) {
        super("PLANO_NAO_ENCONTRADO", "Nenhum plano ativo encontrado para o animal " + animalId);
    }
}
