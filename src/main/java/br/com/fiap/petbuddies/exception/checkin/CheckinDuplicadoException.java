package br.com.fiap.petbuddies.exception.checkin;

import br.com.fiap.petbuddies.exception.ConflitoException;

import java.time.LocalDate;

/** Violação de UK_CHECKIN_ANIMAL_DIA, recusada antes de chegar ao driver. */
public class CheckinDuplicadoException extends ConflitoException {
    public CheckinDuplicadoException(Long animalId, LocalDate dataReferencia, Long itemPlanoCuidadoId) {
        super("CHECKIN_DUPLICADO", "Já existe check-in para o animal " + animalId + " em " + dataReferencia
                + (itemPlanoCuidadoId == null ? " (relato geral)." : " para o item " + itemPlanoCuidadoId + "."));
    }
}
