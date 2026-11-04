package br.com.fiap.petbuddies.exception.prescricao;

import br.com.fiap.petbuddies.exception.ConflitoException;

/** Violação de UK_RECEITA_MODELO_TITULO, recusada antes de chegar ao driver. */
public class TituloReceitaModeloDuplicadoException extends ConflitoException {
    public TituloReceitaModeloDuplicadoException(String titulo) {
        super("TITULO_RECEITA_MODELO_DUPLICADO", "A clínica já tem uma receita-modelo com o título: " + titulo);
    }
}
