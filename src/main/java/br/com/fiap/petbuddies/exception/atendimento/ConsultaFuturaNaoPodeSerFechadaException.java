package br.com.fiap.petbuddies.exception.atendimento;

import br.com.fiap.petbuddies.exception.ConflitoException;

import java.time.LocalDate;

public class ConsultaFuturaNaoPodeSerFechadaException extends ConflitoException {
    public ConsultaFuturaNaoPodeSerFechadaException(Long id, LocalDate data) {
        super("CONSULTA_FUTURA", "Só dá para fechar a consulta no dia dela (consulta " + id + " é de " + data + ").");
    }
}
