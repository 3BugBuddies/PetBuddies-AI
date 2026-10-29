package br.com.fiap.petbuddies.domain.enums.atendimento;

// CK_CONSULTA_TIPO
public enum TipoConsulta {
    ROTINA("Rotina"),
    VACINACAO("Vacinação"),
    EXAME("Exame"),
    RETORNO("Retorno"),
    EMERGENCIA("Emergência");

    private final String rotulo;

    TipoConsulta(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
