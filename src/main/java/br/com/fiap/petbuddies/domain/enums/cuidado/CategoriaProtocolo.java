package br.com.fiap.petbuddies.domain.enums.cuidado;

// Nao admite TRATAMENTO de proposito — tratamento nasce de prescricao, nao tem molde.
public enum CategoriaProtocolo {
    PREVENTIVO("Preventivo"),
    POS_CIRURGICO("Pós-cirúrgico");

    private final String rotulo;

    CategoriaProtocolo(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
