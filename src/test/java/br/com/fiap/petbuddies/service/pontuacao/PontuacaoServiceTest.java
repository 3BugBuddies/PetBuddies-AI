package br.com.fiap.petbuddies.service.pontuacao;

import br.com.fiap.petbuddies.domain.entity.EnvioPontuacaoEntity;
import br.com.fiap.petbuddies.domain.enums.pontuacao.StatusEnvioPontuacao;
import br.com.fiap.petbuddies.domain.event.ConsultaRealizada;
import br.com.fiap.petbuddies.domain.repository.EnvioPontuacaoRepository;
import br.com.fiap.petbuddies.dto.pontuacao.PontuacaoConsultaRequest;
import br.com.fiap.petbuddies.infrastructure.client.PontuacaoClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PontuacaoServiceTest {

    private static final Long CONSULTA_ID = 42L;

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
