package br.com.fiap.petbuddies.service.pontuacao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

// Leva ao .NET as consultas que ficaram PENDENTE porque ele estava fora do ar no fechamento.
@Component
public class ReenvioPontuacaoAgendado {

    private static final Logger log = LoggerFactory.getLogger(ReenvioPontuacaoAgendado.class);

    private final EnvioPontuacaoGravador gravador;
    private final PontuacaoService pontuacaoService;

    public ReenvioPontuacaoAgendado(EnvioPontuacaoGravador gravador, PontuacaoService pontuacaoService) {
        this.gravador = gravador;
        this.pontuacaoService = pontuacaoService;
    }

    @Scheduled(
            initialDelayString = "${petbuddies.pontuacao.reenvio-inicial-ms:60000}",
            fixedDelayString = "${petbuddies.pontuacao.reenvio-ms:300000}")
    public void reenviarPendentes() {
        List<Long> pendentes = gravador.pendentes();
        if (pendentes.isEmpty()) {
            return;
        }

        log.info("[PONTUACAO] reenviando {} consulta(s) pendente(s)", pendentes.size());
        for (Long consultaId : pendentes) {
            try {
                pontuacaoService.enviar(consultaId);
            } catch (Exception e) {
                log.warn("[PONTUACAO] consultaId={} falhou no reenvio", consultaId, e);
            }
        }
    }
}
