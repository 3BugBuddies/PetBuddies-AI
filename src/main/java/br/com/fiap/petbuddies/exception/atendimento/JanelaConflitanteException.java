package br.com.fiap.petbuddies.exception.atendimento;

import java.time.LocalDateTime;

/** Violação de UK_JANELA_VET_INICIO, recusada antes de chegar ao driver. */
public class JanelaConflitanteException extends RuntimeException {
    public JanelaConflitanteException(Long veterinarioId, LocalDateTime dataHoraInicio) {
        super("O veterinário " + veterinarioId + " já tem uma janela de atendimento às " + dataHoraInicio + ".");
    }
}
