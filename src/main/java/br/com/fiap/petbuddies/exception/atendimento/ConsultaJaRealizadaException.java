package br.com.fiap.petbuddies.exception.atendimento;

import br.com.fiap.petbuddies.exception.ConflitoException;

/** Regra de cancelamento: consulta REALIZADA é fato consumado e não volta a AGENDADA nem a CANCELADA. */
public class ConsultaJaRealizadaException extends ConflitoException {
    public ConsultaJaRealizadaException(Long id) {
        super("CONSULTA_JA_REALIZADA", "A consulta " + id + " já foi realizada e não pode ser cancelada.");
    }
}
