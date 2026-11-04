package br.com.fiap.petbuddies.exception.pontuacao;

import br.com.fiap.petbuddies.exception.AcessoNegadoException;

public class PontuacaoDeOutroResponsavelException extends AcessoNegadoException {

    public PontuacaoDeOutroResponsavelException() {
        super("PONTUACAO_DE_OUTRO_RESPONSAVEL", "A pontuação pedida não é do tutor autenticado.");
    }
}
