package br.com.fiap.petbuddies.exception.identidade;

import br.com.fiap.petbuddies.exception.AcessoNegadoException;

public class UsuarioNaoVeterinarioException extends AcessoNegadoException {
    public UsuarioNaoVeterinarioException() {
        super("USUARIO_NAO_VETERINARIO", "O usuário autenticado não está vinculado a um veterinário.");
    }
}
