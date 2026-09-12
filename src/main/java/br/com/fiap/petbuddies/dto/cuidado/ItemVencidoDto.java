package br.com.fiap.petbuddies.dto.cuidado;

import java.time.LocalDate;

/** Uma linha do bloco "precisa de atenção" do painel. */
public record ItemVencidoDto(
        Long animalId,
        String animalNome,
        String tutorNome,
        String cuidado,
        LocalDate dataAlvo,
        long diasAtraso) {
}
