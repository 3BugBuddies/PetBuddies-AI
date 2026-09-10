package br.com.fiap.petbuddies.exception.cuidado;

/** NR_DURACAO_DIAS acima do teto defensivo do motor: recusa antes de materializar item. */
public class DuracaoTratamentoExcedeTetoException extends RuntimeException {
    public DuracaoTratamentoExcedeTetoException(int duracaoDias, int teto) {
        super("Duração de tratamento de " + duracaoDias + " dias excede o teto de " + teto + " dias.");
    }
}
