package br.com.fiap.petbuddies.exception.atendimento;

public class ConsultaNaoEncontradaException extends RuntimeException {
    public ConsultaNaoEncontradaException(Long id) {
        super("Consulta não encontrada para o id: " + id);
    }
}
