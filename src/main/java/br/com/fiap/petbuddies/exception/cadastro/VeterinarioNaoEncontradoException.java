package br.com.fiap.petbuddies.exception.cadastro;

import br.com.fiap.petbuddies.exception.RecursoNaoEncontradoException;

public class VeterinarioNaoEncontradoException extends RecursoNaoEncontradoException {

    private static final String CODIGO = "VETERINARIO_NAO_ENCONTRADO";

    public VeterinarioNaoEncontradoException(Long id) {
        super(CODIGO, "Veterinário não encontrado para o id: " + id);
    }

    public VeterinarioNaoEncontradoException(String crmv) {
        super(CODIGO, "Veterinário não encontrado para o CRMV: " + crmv);
    }
}
