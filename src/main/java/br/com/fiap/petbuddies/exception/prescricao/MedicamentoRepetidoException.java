package br.com.fiap.petbuddies.exception.prescricao;

public class MedicamentoRepetidoException extends RuntimeException {
    public MedicamentoRepetidoException(int indice, String medicamento) {
        super("prescricoes[" + indice + "].medicamento: " + medicamento + " já está na receita.");
    }
}
