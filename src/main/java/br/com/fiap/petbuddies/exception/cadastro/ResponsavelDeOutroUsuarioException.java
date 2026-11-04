package br.com.fiap.petbuddies.exception.cadastro;

import br.com.fiap.petbuddies.exception.AcessoNegadoException;

public class ResponsavelDeOutroUsuarioException extends AcessoNegadoException {
    public ResponsavelDeOutroUsuarioException(Long id) {
        super("RESPONSAVEL_DE_OUTRO_USUARIO", "Responsável " + id + " não é o usuário autenticado.");
    }
}
