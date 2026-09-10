package br.com.fiap.petbuddies.exception.cuidado;

public class ItemPlanoCuidadoNaoEncontradoException extends RuntimeException {
    public ItemPlanoCuidadoNaoEncontradoException(Long id) {
        super("Item de plano de cuidado não encontrado para o id: " + id);
    }
}
