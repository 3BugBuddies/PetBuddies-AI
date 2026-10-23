package br.com.fiap.petbuddies.domain.enums.cuidado;

/**
 * O vocabulario de cuidados, compartilhado pela regra do protocolo e pelo item
 * do plano. E o mesmo enum de propósito: a regra descreve o que o item vai ser.
 */
public enum TipoCuidado {
    VACINACAO("Vacinação"),
    VERMIFUGACAO("Vermifugação"),
    EXAME("Exame"),
    RETORNO("Retorno"),
    CIRURGIA("Cirurgia"),
    MEDICACAO("Medicação"),
    HIGIENE("Higiene");

    private final String rotulo;

    TipoCuidado(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
