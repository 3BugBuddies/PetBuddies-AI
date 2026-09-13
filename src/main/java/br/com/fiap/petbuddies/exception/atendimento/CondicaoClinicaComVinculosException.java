package br.com.fiap.petbuddies.exception.atendimento;

import br.com.fiap.petbuddies.exception.ConflitoException;

/** Recusa a exclusão da condição clínica com vínculo ativo (FK_REGRA_CONDICAO e FK_COBS_CONDICAO). */
public class CondicaoClinicaComVinculosException extends ConflitoException {
    public CondicaoClinicaComVinculosException(Long id, String vinculo) {
        super("CONDICAO_CLINICA_COM_VINCULOS",
                "Condição clínica " + id + " não pode ser removida: é usada em " + vinculo + ". Desative a condição.");
    }
}
