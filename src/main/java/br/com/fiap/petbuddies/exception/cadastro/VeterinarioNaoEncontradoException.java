package br.com.fiap.petbuddies.exception.cadastro;

public class VeterinarioNaoEncontradoException extends RuntimeException {

    public VeterinarioNaoEncontradoException(Long id) {
        super("Veterinário não encontrado para o id: " + id);
    }

    public VeterinarioNaoEncontradoException(String crmv) {
        super("Veterinário não encontrado para o CRMV: " + crmv);
    }
}
