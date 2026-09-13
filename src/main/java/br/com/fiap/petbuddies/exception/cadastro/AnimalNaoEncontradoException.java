package br.com.fiap.petbuddies.exception.cadastro;

import br.com.fiap.petbuddies.exception.RecursoNaoEncontradoException;

public class AnimalNaoEncontradoException extends RecursoNaoEncontradoException {
    public AnimalNaoEncontradoException(Long id) {
        super("ANIMAL_NAO_ENCONTRADO", "Animal não encontrado para o id: " + id);
    }
}
