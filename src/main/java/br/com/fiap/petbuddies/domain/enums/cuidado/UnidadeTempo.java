package br.com.fiap.petbuddies.domain.enums.cuidado;

/** Unidade do deslocamento e do intervalo de recorrencia. */
public enum UnidadeTempo {
    DIAS("dias"),
    SEMANAS("semanas"),
    MESES("meses");

    private final String rotulo;

    UnidadeTempo(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
