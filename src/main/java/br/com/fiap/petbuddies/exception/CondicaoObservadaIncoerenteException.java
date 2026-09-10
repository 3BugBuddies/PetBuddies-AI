package br.com.fiap.petbuddies.exception;

/** Violação de CK_COBS_UM_VALOR ou CK_COBS_CONFIANCA, recusada antes de chegar ao driver. */
public class CondicaoObservadaIncoerenteException extends RuntimeException {
    public CondicaoObservadaIncoerenteException(String message) {
        super(message);
    }
}
