package br.com.fiap.petbuddies.exception;

public abstract class AcessoNegadoException extends RuntimeException {

    private final String codigo;

    protected AcessoNegadoException(String codigo, String mensagem) {
        super(mensagem);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
