package br.com.fiap.petbuddies.dto.paciente;

import br.com.fiap.petbuddies.domain.enums.atendimento.StatusConsulta;
import br.com.fiap.petbuddies.domain.enums.atendimento.TipoConsulta;
import br.com.fiap.petbuddies.domain.enums.checkin.TipoDesfecho;
import br.com.fiap.petbuddies.domain.enums.cuidado.CategoriaPlano;
import br.com.fiap.petbuddies.domain.enums.cuidado.StatusItem;
import br.com.fiap.petbuddies.domain.enums.cuidado.TipoCuidado;
import br.com.fiap.petbuddies.domain.enums.prescricao.OperadorRegra;
import br.com.fiap.petbuddies.domain.enums.prescricao.TipoAcaoRegra;
import br.com.fiap.petbuddies.dto.cadastro.AnimalResponse;
import java.math.BigDecimal;
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
            BigDecimal doseMin,
            BigDecimal doseMax,
            String unidade,
            Integer frequenciaDia,
            Integer duracaoDias,
            LocalDate inicio,
            LocalDate fim,
            List<RegraFicha> regras,
            int registrados,
            int total,
            int percentual) {
    }

    public record RegraFicha(
            String condicao, OperadorRegra operador, BigDecimal limite, String unidade, TipoAcaoRegra acao) {
    }

    /** Efeito de um desfecho do check-in; {@code acao} e {@code dose} só existem em DOSE_CALCULADA. */
    public record EfeitoCheckin(
            String medicamento, TipoDesfecho desfecho, TipoAcaoRegra acao, BigDecimal dose, String unidade) {
    }

    public record CuidadoFicha(String nome, TipoCuidado tipo, LocalDate dataAlvo, StatusItem status, boolean vencido) {
    }

    /**
     * {@code tipo}: CHECKIN, CONSULTA ou PLANO (alimenta o filtro da tela).
     * {@code estilo}: alerta, ok, espera, info ou neutro (cor do selo).
     * CHECKIN preenche {@code detalhe} (o relato), {@code sinais} e {@code efeitos}; CONSULTA, {@code consultaTipo}
     * e {@code consultaStatus} (sem {@code selo}, o status é o selo); PLANO feito, {@code statusItem}.
     */
    public record EntradaLinhaTempo(
            String tipo,
            LocalDateTime quando,
            boolean comHora,
            String selo,
            String estilo,
            String titulo,
            String detalhe,
            String sinais,
            List<EfeitoCheckin> efeitos,
            TipoConsulta consultaTipo,
            StatusConsulta consultaStatus,
            StatusItem statusItem) {
    }
}
