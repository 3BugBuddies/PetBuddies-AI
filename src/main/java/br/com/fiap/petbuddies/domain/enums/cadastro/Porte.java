package br.com.fiap.petbuddies.domain.enums.cadastro;

public enum Porte {
    MINI("Mini"),
    PEQUENO("Pequeno"),
    MEDIO("Médio"),
    GRANDE("Grande"),
    GIGANTE("Gigante");

    private final String rotulo;

    Porte(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
