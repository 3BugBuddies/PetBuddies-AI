package br.com.fiap.petbuddies.exception.atendimento;

import br.com.fiap.petbuddies.exception.RecursoNaoEncontradoException;

public class RegistroAtendimentoNaoEncontradoException extends RecursoNaoEncontradoException {
    public RegistroAtendimentoNaoEncontradoException(Long id) {
        super("REGISTRO_ATENDIMENTO_NAO_ENCONTRADO", "Registro de atendimento não encontrado para o id: " + id);
    }
}
