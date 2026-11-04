package br.com.fiap.petbuddies.domain.enums.prescricao;

// CK_CONDICAO_TP_FONTE, sobre a coluna TP_FONTE_VALOR
public enum TipoFonteValor {
    RELATO("Relato do responsável"),
    ANIMAL("Do animal"),
    HISTORICO("Do histórico");

    private final String rotulo;

    TipoFonteValor(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
