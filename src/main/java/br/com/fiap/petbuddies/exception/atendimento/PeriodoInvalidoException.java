package br.com.fiap.petbuddies.exception.atendimento;

/** Período de consulta com fim antes do início ou maior que o limite aceito. */
public class PeriodoInvalidoException extends RuntimeException {
    public PeriodoInvalidoException(String mensagem) {
        super(mensagem);
    }
}
