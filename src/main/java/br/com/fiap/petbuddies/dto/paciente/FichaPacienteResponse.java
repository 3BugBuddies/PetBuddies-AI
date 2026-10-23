package br.com.fiap.petbuddies.dto.paciente;

import br.com.fiap.petbuddies.domain.enums.cuidado.CategoriaPlano;
import br.com.fiap.petbuddies.domain.enums.cuidado.StatusItem;
import br.com.fiap.petbuddies.domain.enums.cuidado.TipoCuidado;
import br.com.fiap.petbuddies.dto.cadastro.AnimalResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** A ficha do paciente: cabeçalho, planos ativos e a linha do tempo, tudo já resolvido para a tela. */
public record FichaPacienteResponse(
        AnimalResponse animal,
        String idade,
        Long consultaAbertaId,
        List<PlanoFicha> planos,
        List<EntradaLinhaTempo> linhaTempo) {

    /** Plano ativo: tratamentos (um por prescrição) ou cuidados do molde, conforme a categoria. */
    public record PlanoFicha(
            CategoriaPlano categoria,
            List<TratamentoFicha> tratamentos,
            List<CuidadoFicha> cuidados,
            int cuidadosOcultos,
            int cuidadosFeitos) {
    }

    public record TratamentoFicha(
            String medicamento,
            String dose,
            String frequencia,
            LocalDate inicio,
            LocalDate fim,
            List<String> regras,
            int registrados,
            int total,
            int percentual) {
    }

    public record CuidadoFicha(String nome, TipoCuidado tipo, LocalDate dataAlvo, StatusItem status, boolean vencido) {
    }

    /**
     * {@code tipo}: CHECKIN, CONSULTA ou PLANO (alimenta o filtro da tela).
     * {@code estilo}: alerta, ok, espera, info ou neutro (cor do selo).
     */
    public record EntradaLinhaTempo(
            String tipo,
            LocalDateTime quando,
            boolean comHora,
            String selo,
            String estilo,
            String titulo,
            String detalhe) {
    }
}
