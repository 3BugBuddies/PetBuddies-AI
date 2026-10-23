package br.com.fiap.petbuddies.domain.enums.cadastro;

public enum Especie {
    CACHORRO("Cão"),
    GATO("Gato"),
    PASSARO("Pássaro"),
    COELHO("Coelho"),
    HAMSTER("Hamster"),
    OUTRO("Outro");

    private final String rotulo;

    Especie(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
