package br.com.fiap.petbuddies.exception.prescricao;

/** Regra de receita-modelo que o schema não alcança: condição de outra clínica, inativa, ou operador incompatível com o tipo. */
public class ReceitaModeloIncoerenteException extends RuntimeException {
    public ReceitaModeloIncoerenteException(String message) {
        super(message);
    }
}
