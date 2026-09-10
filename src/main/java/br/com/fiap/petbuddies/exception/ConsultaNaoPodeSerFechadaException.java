package br.com.fiap.petbuddies.exception;

import br.com.fiap.petbuddies.domain.enums.StatusConsulta;

/** A consulta não está AGENDADA nem CONFIRMADA — fechar de novo é conflito, não repetição. */
public class ConsultaNaoPodeSerFechadaException extends RuntimeException {
    public ConsultaNaoPodeSerFechadaException(Long id, StatusConsulta statusAtual) {
        super("Consulta " + id + " não pode ser fechada: está " + statusAtual + ".");
    }
}
