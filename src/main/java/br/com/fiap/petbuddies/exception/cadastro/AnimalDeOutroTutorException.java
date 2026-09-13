package br.com.fiap.petbuddies.exception.cadastro;

import br.com.fiap.petbuddies.exception.AcessoNegadoException;

public class AnimalDeOutroTutorException extends AcessoNegadoException {
    public AnimalDeOutroTutorException(Long id) {
        super("ANIMAL_DE_OUTRO_TUTOR", "Animal " + id + " não pertence ao tutor autenticado.");
    }
}
