package br.com.fiap.petbuddies.exception.atendimento;

/** Consulta referenciada não pertence ao animal informado. */
public class ConsultaDeOutroAnimalException extends RuntimeException {
    public ConsultaDeOutroAnimalException(Long consultaId, Long animalId) {
        super("Consulta " + consultaId + " não é do animal " + animalId + ".");
    }
}
