package br.com.fiap.petbuddies.service.pontuacao;

import br.com.fiap.petbuddies.domain.entity.EnvioPontuacaoEntity;
import br.com.fiap.petbuddies.domain.enums.pontuacao.StatusEnvioPontuacao;
import br.com.fiap.petbuddies.domain.event.ConsultaRealizada;
import br.com.fiap.petbuddies.domain.repository.EnvioPontuacaoRepository;
import br.com.fiap.petbuddies.domain.enums.atendimento.EvolucaoClinica;
import br.com.fiap.petbuddies.dto.pontuacao.MemoriaCalculoResponse;
import br.com.fiap.petbuddies.dto.pontuacao.PontuacaoConsultaRequest;
import br.com.fiap.petbuddies.dto.pontuacao.SaldoPontosResponse;
import br.com.fiap.petbuddies.exception.pontuacao.MemoriaCalculoNaoEncontradaException;
import br.com.fiap.petbuddies.exception.pontuacao.PontuacaoDeOutroResponsavelException;
import br.com.fiap.petbuddies.exception.pontuacao.PontuacaoIndisponivelException;
import br.com.fiap.petbuddies.infrastructure.client.PontuacaoClient;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PontuacaoServiceTest {

    private static final Long CONSULTA_ID = 42L;
    private static final String TOKEN_TUTOR = "Bearer token-do-tutor";

    private EnvioPontuacaoRepository envioRepository;
    private EnvioPontuacaoGravador gravador;
    private PontuacaoClient client;
    private ApplicationEventPublisher eventos;
    private PontuacaoService service;

    private static PontuacaoConsultaRequest requisicao() {
        return new PontuacaoConsultaRequest(CONSULTA_ID, 1L, 3L, 1L,
                LocalDateTime.of(2026, 10, 1, 14, 30), null,
                20, 18, 30, 17, 0, null);
    }

    @BeforeEach
    void preparar() {
        envioRepository = mock(EnvioPontuacaoRepository.class);
        gravador = mock(EnvioPontuacaoGravador.class);
        client = mock(PontuacaoClient.class);
        eventos = mock(ApplicationEventPublisher.class);
        service = new PontuacaoService(envioRepository, gravador, client, eventos);
    }

    @Test
    void registrar_consultaNova_gravaPendenteEPublicaOEvento() {
        when(envioRepository.existsByConsultaId(CONSULTA_ID)).thenReturn(false);

        service.registrar(CONSULTA_ID);

        verify(envioRepository).save(argThat((EnvioPontuacaoEntity envio) ->
                CONSULTA_ID.equals(envio.getConsultaId()) && envio.getStatus() == StatusEnvioPontuacao.PENDENTE));
        verify(eventos).publishEvent(new ConsultaRealizada(CONSULTA_ID));
    }

    @Test
    void registrar_envioJaExiste_naoDuplica() {
        when(envioRepository.existsByConsultaId(CONSULTA_ID)).thenReturn(true);

        service.registrar(CONSULTA_ID);

        verify(envioRepository, never()).save(any());
    }

    @Test
    void enviar_netRespondeOk_marcaEnviado() {
        when(gravador.montarRequisicao(CONSULTA_ID)).thenReturn(Optional.of(requisicao()));

        service.enviar(CONSULTA_ID);

        verify(client).pontuar(requisicao());
        verify(gravador).marcarEnviado(CONSULTA_ID);
    }

    @Test
    void enviar_semEnvioPendente_naoChamaONet() {
        when(gravador.montarRequisicao(CONSULTA_ID)).thenReturn(Optional.empty());

        service.enviar(CONSULTA_ID);

        verifyNoInteractions(client);
    }

    @Test
    void enviar_netRecusaComBadRequest_marcaRecusado() {
        when(gravador.montarRequisicao(CONSULTA_ID)).thenReturn(Optional.of(requisicao()));
        doThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad Request", HttpHeaders.EMPTY,
                "Doses com check-in não podem passar das doses previstas.".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8))
                .when(client).pontuar(any());

        service.enviar(CONSULTA_ID);

        verify(gravador).marcarRecusado(eq(CONSULTA_ID), anyString());
        verify(gravador, never()).marcarEnviado(any());
    }

    @Test
    void enviar_tokenRecusado_ficaParaOReenvio() {
        when(gravador.montarRequisicao(CONSULTA_ID)).thenReturn(Optional.of(requisicao()));
        doThrow(HttpClientErrorException.create(HttpStatus.UNAUTHORIZED, "Unauthorized", HttpHeaders.EMPTY,
                new byte[0], StandardCharsets.UTF_8))
                .when(client).pontuar(any());

        service.enviar(CONSULTA_ID);

        verify(gravador).registrarFalha(eq(CONSULTA_ID), anyString());
        verify(gravador, never()).marcarRecusado(any(), any());
    }

    @Test
    void enviar_netForaDoAr_registraFalhaEContinuaPendente() {
        when(gravador.montarRequisicao(CONSULTA_ID)).thenReturn(Optional.of(requisicao()));
        doThrow(new ResourceAccessException("Connection refused")).when(client).pontuar(any());

        service.enviar(CONSULTA_ID);

        verify(gravador).registrarFalha(CONSULTA_ID, "Connection refused");
        verify(gravador, never()).marcarEnviado(any());
    }

    @Test
    void saldo_netResponde_devolveOSaldoComOTokenDeQuemPediu() {
        when(client.saldo(TOKEN_TUTOR)).thenReturn(new SaldoPontosResponse(1L, 120));

        SaldoPontosResponse saldo = service.saldo(TOKEN_TUTOR);

        assertEquals(120, saldo.saldo());
    }

    @Test
    void extrato_netRecusaOToken_viraAcessoNegado() {
        when(client.extrato(TOKEN_TUTOR)).thenThrow(erroDoNet(HttpStatus.FORBIDDEN));

        assertThrows(PontuacaoDeOutroResponsavelException.class, () -> service.extrato(TOKEN_TUTOR));
    }

    @Test
    void memoria_consultaSemMemoria_viraNaoEncontrada() {
        when(client.memoria(CONSULTA_ID, TOKEN_TUTOR)).thenThrow(erroDoNet(HttpStatus.NOT_FOUND));

        assertThrows(MemoriaCalculoNaoEncontradaException.class, () -> service.memoria(CONSULTA_ID, TOKEN_TUTOR));
    }

    @Test
    void memoria_mongoDoNetForaDoAr_viraIndisponivel() {
        when(client.memoria(CONSULTA_ID, TOKEN_TUTOR)).thenThrow(HttpServerErrorException.create(
                HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable", HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8));

        assertThrows(PontuacaoIndisponivelException.class, () -> service.memoria(CONSULTA_ID, TOKEN_TUTOR));
    }

    @Test
    void saldo_netForaDoAr_viraIndisponivel() {
        when(client.saldo(TOKEN_TUTOR)).thenThrow(new ResourceAccessException("Connection refused"));

        assertThrows(PontuacaoIndisponivelException.class, () -> service.saldo(TOKEN_TUTOR));
    }

    @Test
    void saldo_segredoDoJwtDiferenteNosServicos_viraIndisponivelENaoAcessoNegado() {
        when(client.saldo(TOKEN_TUTOR)).thenThrow(erroDoNet(HttpStatus.UNAUTHORIZED));

        assertThrows(PontuacaoIndisponivelException.class, () -> service.saldo(TOKEN_TUTOR));
    }

    @Test
    void memoria_jsonDoNet_comDatasDoMongoEmUtc_eLidaPeloJava() throws Exception {
        // Mesmo ajuste do Jackson do Spring Boot: campo desconhecido não derruba a leitura.
        ObjectMapper mapper = new ObjectMapper()
                .findAndRegisterModules()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String json = """
                {"consultaId":42,"lancamentoId":7,"responsavelId":1,"animalId":3,"clinicaId":1,
                 "periodoDe":"2026-09-01T13:00:00Z","periodoAte":"2026-10-01T17:30:00.1234567Z",
                 "dosesPrevistas":20,"dosesComCheckin":18,"diasNoPeriodo":30,"diasComCheckin":17,
                 "acionamentosClinica":1,"evolucao":"MELHOROU",
                 "parcelas":[{"gesto":"CONSULTA_REALIZADA","quantidade":1,"pontosPorUnidade":10,"pontos":10,
                              "regraId":4,"inicioVigencia":"2026-01-01","observacao":"Consulta realizada."}],
                 "total":10,"calculadoEm":"2026-10-01T17:31:02Z","campoNovo":true}
                """;

        MemoriaCalculoResponse memoria = mapper.readValue(json, MemoriaCalculoResponse.class);

        assertEquals(EvolucaoClinica.MELHOROU, memoria.evolucao());
        assertEquals(LocalDateTime.of(2026, 9, 1, 13, 0), memoria.periodoDe());
        assertEquals(10, memoria.parcelas().get(0).pontos());
    }

    private static HttpClientErrorException erroDoNet(HttpStatus status) {
        return HttpClientErrorException.create(status, status.getReasonPhrase(), HttpHeaders.EMPTY,
                new byte[0], StandardCharsets.UTF_8);
    }

    @Test
    void requisicao_serializadaComoOSpringFaz_temOsNomesQueONetEspera() throws Exception {
        // Mesmo ajuste do Jackson do Spring Boot: datas em ISO, não em timestamp numérico.
        ObjectMapper mapper = new ObjectMapper()
                .findAndRegisterModules()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        JsonNode json = mapper.readTree(mapper.writeValueAsString(requisicao()));

        assertEquals(42, json.get("consultaId").asInt());
        assertEquals("2026-10-01T14:30:00", json.get("dataConsulta").asText());
        assertEquals(18, json.get("dosesComCheckin").asInt());
        assertEquals(17, json.get("diasComCheckin").asInt());
        assertEquals(0, json.get("acionamentosClinica").asInt());
    }
}
