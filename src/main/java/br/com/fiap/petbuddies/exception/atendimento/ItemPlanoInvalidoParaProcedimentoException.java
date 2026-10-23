package br.com.fiap.petbuddies.exception.atendimento;

/** Item do plano apontado pelo procedimento do fechamento não pode ser baixado por ele. */
public class ItemPlanoInvalidoParaProcedimentoException extends RuntimeException {
    public ItemPlanoInvalidoParaProcedimentoException(String mensagem) {
        super(mensagem);
    }
}
