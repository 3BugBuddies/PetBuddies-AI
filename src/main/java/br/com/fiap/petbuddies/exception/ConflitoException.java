package br.com.fiap.petbuddies.exception;

public abstract class ConflitoException extends RuntimeException {

    private final String codigo;

    protected ConflitoException(String codigo, String mensagem) {
        super(mensagem);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
