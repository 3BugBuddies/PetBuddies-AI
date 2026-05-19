package br.com.fiap.petbuddies.exception;

public class PlanoNaoEncontradoException extends RuntimeException {
    public PlanoNaoEncontradoException(Long petNetApiAnimalId) {
        super("Nenhum plano ativo encontrado para o animal " + petNetApiAnimalId);
    }
}
