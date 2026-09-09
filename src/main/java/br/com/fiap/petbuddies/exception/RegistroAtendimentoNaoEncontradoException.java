package br.com.fiap.petbuddies.exception;

public class RegistroAtendimentoNaoEncontradoException extends RuntimeException {
    public RegistroAtendimentoNaoEncontradoException(Long id) {
        super("Registro de atendimento não encontrado para o id: " + id);
    }
}
