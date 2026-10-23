package br.com.fiap.petbuddies.dto.paciente;

/** Os quatro filtros da lista de pacientes; o parâmetro da URL é o nome do enum. */
public enum FiltroPaciente {
    TODOS("Todos"),
    TRATAMENTO("Em tratamento"),
    PREVENTIVO("Preventivo"),
    SEM_PLANO("Sem plano");

    private final String rotulo;

    FiltroPaciente(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
