package br.com.fiap.petbuddies.service.pontuacao;

import br.com.fiap.petbuddies.domain.event.ConsultaRealizada;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PontuacaoListener {

    private static final Logger log = LoggerFactory.getLogger(PontuacaoListener.class);

    private final PontuacaoService pontuacaoService;

    public PontuacaoListener(PontuacaoService pontuacaoService) {
        this.pontuacaoService = pontuacaoService;
    }

    // @Async: a resposta do fechamento não espera o .NET, que pode levar até o timeout do client.
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoRealizarConsulta(ConsultaRealizada evento) {
        try {
            pontuacaoService.enviar(evento.consultaId());
        } catch (Exception e) {
            // A consulta já foi commitada e o envio segue PENDENTE: o reenvio agendado tenta de novo.
            log.warn("[PONTUACAO] consultaId={} sem envio imediato", evento.consultaId(), e);
        }
    }
}
