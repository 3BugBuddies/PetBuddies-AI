package br.com.fiap.petbuddies.exception;

/** Regra de cancelamento: consulta REALIZADA é fato consumado e não volta a AGENDADA nem a CANCELADA. */
public class ConsultaJaRealizadaException extends RuntimeException {
    public ConsultaJaRealizadaException(Long id) {
        super("A consulta " + id + " já foi realizada e não pode ser cancelada.");
    }
}
