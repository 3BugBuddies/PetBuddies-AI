package br.com.fiap.petbuddies.domain.enums.cuidado;

// Copiada do protocolo ao aplicar; TRATAMENTO nao existe em CategoriaProtocolo por nao ter molde.
public enum CategoriaPlano {
    PREVENTIVO("Preventivo"),
    POS_CIRURGICO("Pós-cirúrgico"),
    TRATAMENTO("Tratamento");

    private final String rotulo;

    CategoriaPlano(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
