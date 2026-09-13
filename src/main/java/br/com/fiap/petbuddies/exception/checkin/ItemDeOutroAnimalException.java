package br.com.fiap.petbuddies.exception.checkin;

/** Item do plano relatado no check-in pertence a outro animal. */
public class ItemDeOutroAnimalException extends RuntimeException {
    public ItemDeOutroAnimalException(Long itemId, Long animalId) {
        super("Item " + itemId + " do plano não é do animal " + animalId + ".");
    }
}
