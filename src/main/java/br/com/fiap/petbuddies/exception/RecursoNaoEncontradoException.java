package br.com.fiap.petbuddies.exception;

public abstract class RecursoNaoEncontradoException extends RuntimeException {

    private final String codigo;

    protected RecursoNaoEncontradoException(String codigo, String mensagem) {
        super(mensagem);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
