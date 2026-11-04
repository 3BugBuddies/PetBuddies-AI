package br.com.fiap.petbuddies.domain.enums.cuidado;

// Data-base do deslocamento da regra: nascimento, cirurgia, ou ultima vez que o cuidado foi feito.
public enum TipoDataBase {
    NASCIMENTO("do nascimento"),
    DATA_CIRURGIA("da cirurgia"),
    ULTIMA_REALIZACAO("da última vez");

    private final String rotulo;

    TipoDataBase(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
