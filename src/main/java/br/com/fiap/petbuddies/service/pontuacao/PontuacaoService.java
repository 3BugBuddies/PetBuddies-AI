package br.com.fiap.petbuddies.service.pontuacao;

import br.com.fiap.petbuddies.domain.entity.EnvioPontuacaoEntity;
import br.com.fiap.petbuddies.domain.event.ConsultaRealizada;
import br.com.fiap.petbuddies.domain.repository.EnvioPontuacaoRepository;
import br.com.fiap.petbuddies.dto.pontuacao.PontuacaoConsultaRequest;
import br.com.fiap.petbuddies.infrastructure.client.PontuacaoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

/**
 * Leva a consulta realizada até a pontuação do responsável no .NET. O cálculo
 * e as regras ficam lá; daqui sai só o resumo do período.
 */
@Service
public class PontuacaoService {

    private static final Logger log = LoggerFactory.getLogger(PontuacaoService.class);

    private final EnvioPontuacaoRepository envioRepository;
    private final EnvioPontuacaoGravador gravador;
    private final PontuacaoClient pontuacaoClient;
    private final ApplicationEventPublisher eventos;

    public PontuacaoService(
            EnvioPontuacaoRepository envioRepository,
            EnvioPontuacaoGravador gravador,
            PontuacaoClient pontuacaoClient,
            ApplicationEventPublisher eventos) {
        this.envioRepository = envioRepository;
        this.gravador = gravador;
        this.pontuacaoClient = pontuacaoClient;
        this.eventos = eventos;
    }

    /**
     * Chamado dentro da transação do fechamento: o envio nasce PENDENTE junto com a
     * consulta REALIZADA, e o rollback de um desfaz o outro.
     */
    @Transactional
    public void registrar(Long consultaId) {
        if (!envioRepository.existsByConsultaId(consultaId)) {
            EnvioPontuacaoEntity envio = new EnvioPontuacaoEntity();
            envio.setConsultaId(consultaId);
            envioRepository.save(envio);
        }
        eventos.publishEvent(new ConsultaRealizada(consultaId));
    }

    // Sem @Transactional de propósito: a chamada HTTP fica entre as transações curtas do gravador.
    public void enviar(Long consultaId) {
        Optional<PontuacaoConsultaRequest> requisicao = gravador.montarRequisicao(consultaId);
        if (requisicao.isEmpty()) {
            return;
        }

        try {
            pontuacaoClient.pontuar(requisicao.get());
            gravador.marcarEnviado(consultaId);
            log.info("[PONTUACAO] consultaId={} pontuada no .NET", consultaId);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST)) {
                // O .NET recusou os dados: reenviar o mesmo corpo daria o mesmo 400.
                gravador.marcarRecusado(consultaId, e.getStatusCode() + " " + e.getResponseBodyAsString());
                log.warn("[PONTUACAO] consultaId={} recusada pelo .NET: {}", consultaId, e.getResponseBodyAsString());
            } else {
                // 401/403 (segredo do JWT diferente) e 429 se resolvem do lado de lá: fica para o reenvio.
                gravador.registrarFalha(consultaId, e.getStatusCode() + " " + e.getResponseBodyAsString());
                log.warn("[PONTUACAO] consultaId={} sem pontuar ({}); fica para o reenvio", consultaId, e.getStatusCode());
            }
        } catch (RestClientException e) {
            gravador.registrarFalha(consultaId, e.getMessage());
            log.warn("[PONTUACAO] consultaId={} sem pontuar, .NET indisponível; fica para o reenvio: {}",
                    consultaId, e.getMessage());
        }
    }
}
