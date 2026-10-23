package br.com.fiap.petbuddies.domain.enums.atendimento;

// CK_PROCEDIMENTO_TIPO
public enum TipoProcedimento {
    VACINACAO("Vacinação"),
    VERMIFUGACAO("Vermifugação"),
    EXAME_LABORATORIAL("Exame laboratorial"),
    EXAME_IMAGEM("Exame de imagem"),
    CIRURGIA("Cirurgia"),
    INTERNACAO("Internação"),
    OUTRO("Outro");

    private final String rotulo;

    TipoProcedimento(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
