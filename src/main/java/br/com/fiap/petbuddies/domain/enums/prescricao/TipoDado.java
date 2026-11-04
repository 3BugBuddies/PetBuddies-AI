package br.com.fiap.petbuddies.domain.enums.prescricao;

// CK_CONDICAO_TP_VALOR, sobre a coluna TP_DADO
public enum TipoDado {
    BOOLEANO("Sim/não"),
    NUMERICO("Número");

    private final String rotulo;

    TipoDado(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
