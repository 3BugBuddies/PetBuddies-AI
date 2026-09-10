package br.com.fiap.petbuddies.exception;

import java.time.LocalDateTime;

/** Regra de agendamento: uma janela cujo horário de início já passou não recebe consulta nova. */
public class JanelaNoPassadoException extends RuntimeException {
    public JanelaNoPassadoException(LocalDateTime dataHoraInicio) {
        super("A janela de atendimento das " + dataHoraInicio + " já passou e não recebe agendamento.");
    }
}
