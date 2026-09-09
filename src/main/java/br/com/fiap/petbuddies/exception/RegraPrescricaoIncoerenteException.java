package br.com.fiap.petbuddies.exception;

/** Violação de CK_REGRA_COERENCIA, recusada antes de chegar ao driver. */
public class RegraPrescricaoIncoerenteException extends RuntimeException {
    public RegraPrescricaoIncoerenteException(String message) {
        super(message);
    }
}
