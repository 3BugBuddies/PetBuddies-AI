package br.com.fiap.petbuddies.dto.atendimento;

import java.time.LocalDateTime;

/** Um horário aberto que ainda não recebeu consulta. */
public record JanelaLivreDto(Long id, LocalDateTime inicio, Long veterinarioId, String veterinarioNome) {
}
