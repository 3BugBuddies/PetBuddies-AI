package br.com.fiap.petbuddies.exception.cuidado;

import br.com.fiap.petbuddies.exception.RecursoNaoEncontradoException;

public class ModeloPlanoNaoEncontradoException extends RecursoNaoEncontradoException {
    public ModeloPlanoNaoEncontradoException(Long id) {
        super("MODELO_PLANO_NAO_ENCONTRADO", "Modelo de plano não encontrado para o id: " + id);
    }
}
