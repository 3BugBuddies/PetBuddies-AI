package br.com.fiap.petbuddies.domain.enums.cuidado;

/**
 * Estado de um item do plano. Nao confundir com {@link StatusPlano}, que descreve
 * o curso inteiro: os dois compartilham CANCELADO, entao trocar um pelo outro
 * compila.
 */
public enum StatusItem {
    PENDENTE("Pendente", "Pendente"),
    REALIZADO("Feito", "Dose dada"),
    SUSPENSO("Suspenso", "Sem dose"),
    ENCAMINHADO("Encaminhado", "Clínica acionada"),
    CANCELADO("Cancelado", "Cancelado"),
    ATRASADO("Atrasado", "Atrasado");

    private final String rotulo;
    private final String rotuloDose;

    StatusItem(String rotulo, String rotuloDose) {
        this.rotulo = rotulo;
        this.rotuloDose = rotuloDose;
    }

    public String getRotulo() {
        return rotulo;
    }

    public String getRotuloDose() {
        return rotuloDose;
    }
}
