package br.com.fiap.petbuddies.exception.checkin;

import java.time.LocalDate;

/** Violação de UK_CHECKIN_ANIMAL_DIA, recusada antes de chegar ao driver. */
public class CheckinDuplicadoException extends RuntimeException {
    public CheckinDuplicadoException(Long animalId, LocalDate dataReferencia, Long itemPlanoCuidadoId) {
        super("Já existe check-in para o animal " + animalId + " em " + dataReferencia
                + (itemPlanoCuidadoId == null ? " (relato geral)." : " para o item " + itemPlanoCuidadoId + "."));
    }
}
