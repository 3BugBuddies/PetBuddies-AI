package br.com.fiap.petbuddies.exception.atendimento;

/** Registro de atendimento referenciado não pertence ao animal informado. */
public class RegistroDeOutroAnimalException extends RuntimeException {
    public RegistroDeOutroAnimalException(Long registroAtendimentoId, Long animalId) {
        super("Registro de atendimento " + registroAtendimentoId + " não é do animal " + animalId + ".");
    }
}
