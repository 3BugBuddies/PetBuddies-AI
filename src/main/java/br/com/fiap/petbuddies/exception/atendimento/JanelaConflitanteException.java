package br.com.fiap.petbuddies.exception.atendimento;

import br.com.fiap.petbuddies.exception.ConflitoException;

import java.time.LocalDateTime;

/** Violação de UK_JANELA_VET_INICIO, recusada antes de chegar ao driver. */
public class JanelaConflitanteException extends ConflitoException {
    public JanelaConflitanteException(Long veterinarioId, LocalDateTime dataHoraInicio) {
        super("JANELA_CONFLITANTE", "O veterinário " + veterinarioId + " já tem uma janela de atendimento às " + dataHoraInicio + ".");
    }
}
