package br.com.fiap.petbuddies.exception.cadastro;

import br.com.fiap.petbuddies.exception.ConflitoException;

/** Recusa a exclusão da clínica com vínculo ativo (FK_VETERINARIO_CLINICA e FK_CONDICAO_CLINICA). */
public class ClinicaComVinculosException extends ConflitoException {
    public ClinicaComVinculosException(Long id, String vinculo) {
        super("CLINICA_COM_VINCULOS", "Clínica " + id + " não pode ser removida: possui " + vinculo + ".");
    }
}
