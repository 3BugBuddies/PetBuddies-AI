package br.com.fiap.petbuddies.exception.cadastro;

public class RegistroIncompletoException extends RuntimeException {
    public RegistroIncompletoException(String mensagem) {
        super(mensagem);
    }
}
