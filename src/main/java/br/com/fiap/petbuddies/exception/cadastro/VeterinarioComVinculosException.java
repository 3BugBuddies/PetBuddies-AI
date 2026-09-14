package br.com.fiap.petbuddies.exception.cadastro;

import br.com.fiap.petbuddies.exception.ConflitoException;

/** Recusa a exclusão do veterinário com vínculo ativo (FK_USUARIO_VETERINARIO e as demais de T_PB_VETERINARIO). */
public class VeterinarioComVinculosException extends ConflitoException {
    public VeterinarioComVinculosException(Long id, String vinculo) {
        super("VETERINARIO_COM_VINCULOS",
                "Veterinário " + id + " não pode ser removido: possui " + vinculo + ". Inative o cadastro.");
    }
}
