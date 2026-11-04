package br.com.fiap.petbuddies.exception.pontuacao;

import br.com.fiap.petbuddies.exception.RecursoNaoEncontradoException;

public class MemoriaCalculoNaoEncontradaException extends RecursoNaoEncontradoException {

    public MemoriaCalculoNaoEncontradaException(Long consultaId) {
        super("MEMORIA_CALCULO_NAO_ENCONTRADA", "Memória de cálculo não encontrada para a consulta: " + consultaId);
    }
}
