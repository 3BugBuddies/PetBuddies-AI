package br.com.fiap.petbuddies.domain.enums.atendimento;

// CK_REGATEND_EVOLUCAO — como o animal chegou nesta consulta em relação à anterior.
public enum EvolucaoClinica {
    MELHOROU("melhorou"),
    ESTAVEL("estável"),
    PIOROU("piorou");

    private final String rotulo;

    EvolucaoClinica(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
