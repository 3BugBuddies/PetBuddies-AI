package br.com.fiap.petbuddies.exception.cadastro;

import br.com.fiap.petbuddies.exception.RecursoNaoEncontradoException;

public class ResponsavelNaoEncontradoException extends RecursoNaoEncontradoException {
    public ResponsavelNaoEncontradoException(Long id) {
        super("RESPONSAVEL_NAO_ENCONTRADO", "Responsável não encontrado para o id: " + id);
    }
}
