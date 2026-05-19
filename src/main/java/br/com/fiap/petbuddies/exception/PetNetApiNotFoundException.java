package br.com.fiap.petbuddies.exception;

public class PetNetApiNotFoundException extends RuntimeException {
    public PetNetApiNotFoundException(String message) {
        super(message);
    }
}
