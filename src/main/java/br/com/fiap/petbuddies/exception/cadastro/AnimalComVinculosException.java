package br.com.fiap.petbuddies.exception.cadastro;

import br.com.fiap.petbuddies.exception.ConflitoException;

/** Recusa a exclusão do animal com vínculo ativo (FK_CONSULTA_ANIMAL e as demais de T_PB_ANIMAL). */
public class AnimalComVinculosException extends ConflitoException {
    public AnimalComVinculosException(Long id, String vinculo) {
        super("ANIMAL_COM_VINCULOS", "Animal " + id + " não pode ser removido: possui " + vinculo + ".");
    }
}
