package br.com.fiap.petbuddies.domain.enums.prescricao;

// CK_REGRA_ACAO
public enum TipoAcaoRegra {
    DOSE_MIN("dose mínima"),
    DOSE_MAX("dose máxima"),
    DOSE_PADRAO("dose padrão"),
    ACIONAR_CLINICA("acionar clínica"),
    SUSPENDER("suspender");

    private final String rotulo;

    TipoAcaoRegra(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
