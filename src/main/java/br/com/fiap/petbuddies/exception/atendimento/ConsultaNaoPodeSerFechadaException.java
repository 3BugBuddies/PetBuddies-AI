package br.com.fiap.petbuddies.exception.atendimento;

import br.com.fiap.petbuddies.domain.enums.atendimento.StatusConsulta;
import br.com.fiap.petbuddies.exception.ConflitoException;

/** A consulta não está AGENDADA nem CONFIRMADA — fechar de novo é conflito, não repetição. */
public class ConsultaNaoPodeSerFechadaException extends ConflitoException {
    public ConsultaNaoPodeSerFechadaException(Long id, StatusConsulta statusAtual) {
        super("CONSULTA_NAO_PODE_SER_FECHADA", "Consulta " + id + " não pode ser fechada: está " + statusAtual + ".");
    }
}
