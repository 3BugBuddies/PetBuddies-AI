package br.com.fiap.petbuddies.dto.checkin;

import java.time.LocalDateTime;

/** Um relato do responsável na lista "Para ler hoje": o texto e o que a regra fez com ele. */
public record RelatoParaLerDto(
        Long checkinId,
        Long animalId,
        String animalNome,
        String responsavelNome,
        LocalDateTime registradoEm,
        String narrativa,
        Tipo tipo,
        String resumo) {

    /** Na ordem em que a lista é mostrada: o alarme primeiro. */
    public enum Tipo {
        ALARME("alarme"),
        SEM_REGRA("sem regra"),
        DOSE("dose");

        private final String rotulo;

        Tipo(String rotulo) {
            this.rotulo = rotulo;
        }

        public String getRotulo() {
            return rotulo;
        }
    }
}
