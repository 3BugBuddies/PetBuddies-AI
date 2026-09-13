package br.com.fiap.petbuddies.exception.cadastro;

import br.com.fiap.petbuddies.exception.ConflitoException;

/** Recusa a exclusão do responsável com vínculo ativo (FK_ANIMAL_RESPONSAVEL e FK_USUARIO_RESPONSAVEL). */
public class ResponsavelComVinculosException extends ConflitoException {
    public ResponsavelComVinculosException(Long id, String vinculo) {
        super("RESPONSAVEL_COM_VINCULOS", "Responsável " + id + " não pode ser removido: possui " + vinculo + ".");
    }
}
