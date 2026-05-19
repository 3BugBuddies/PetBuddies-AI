package br.com.fiap.petbuddies.domain.enums;

public enum ClassificacaoRisco {
    BAIXO, MEDIO, ALTO;

    public static ClassificacaoRisco classificar(int score) {
        if (score <= 30) return BAIXO;
        if (score <= 60) return MEDIO;
        return ALTO;
    }
}
