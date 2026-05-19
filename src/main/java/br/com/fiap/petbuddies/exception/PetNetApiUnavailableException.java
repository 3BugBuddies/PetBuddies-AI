package br.com.fiap.petbuddies.exception;

public class PetNetApiUnavailableException extends RuntimeException {
    public PetNetApiUnavailableException(Throwable cause) {
        super("VetAPI indisponível", cause);
    }
}
