package br.com.fiap.petbuddies.exception.atendimento;

import br.com.fiap.petbuddies.exception.RecursoNaoEncontradoException;

public class ConsultaNaoEncontradaException extends RecursoNaoEncontradoException {
    public ConsultaNaoEncontradaException(Long id) {
        super("CONSULTA_NAO_ENCONTRADA", "Consulta não encontrada para o id: " + id);
    }
}
