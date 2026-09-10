package br.com.fiap.petbuddies.exception.cuidado;

public class PlanoNaoEncontradoException extends RuntimeException {
    public PlanoNaoEncontradoException(Long animalId) {
        super("Nenhum plano ativo encontrado para o animal " + animalId);
    }
}
