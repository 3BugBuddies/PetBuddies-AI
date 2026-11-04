package br.com.fiap.petbuddies.domain.enums.atendimento;

// CK_CONSULTA_STATUS
public enum StatusConsulta {
    AGENDADA("agendada"),
    CONFIRMADA("confirmada"),
    REALIZADA("atendida"),
    CANCELADA("cancelada"),
    NAO_COMPARECEU("não compareceu");

    private final String rotulo;

    StatusConsulta(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
