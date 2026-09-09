package br.com.fiap.petbuddies.exception;

public class CondicaoClinicaNaoEncontradaException extends RuntimeException {

    public CondicaoClinicaNaoEncontradaException(Long id) {
        super("Condição clínica não encontrada para o id: " + id);
    }

    public CondicaoClinicaNaoEncontradaException(Long clinicaId, String codigo) {
        super("Condição clínica não encontrada para o código " + codigo + " na clínica " + clinicaId);
    }
}
