package br.com.fiap.petbuddies.dto.cuidado;

/**
 * Os números do painel da clínica. {@code adesaoPercentual} é nulo quando não há
 * base para calcular — nenhum cuidado com data-alvo na janela.
 */
public record ResumoAcompanhamento(
        long planosAtivos,
        long animaisComPlano,
        long itensVencidos,
        long animaisComVencido,
        Integer adesaoPercentual,
        long realizadosNaJanela,
        long previstosNaJanela,
        int diasDaJanela) {
}
