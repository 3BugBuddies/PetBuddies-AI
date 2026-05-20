package br.com.fiap.petbuddies.service;

import br.com.fiap.petbuddies.domain.entity.EventoPlanoEntity;
import br.com.fiap.petbuddies.domain.entity.NotificacaoEntity;
import br.com.fiap.petbuddies.domain.enums.CanalNotificacao;
import br.com.fiap.petbuddies.domain.enums.StatusEventoPlano;
import br.com.fiap.petbuddies.domain.enums.StatusNotificacao;
import br.com.fiap.petbuddies.domain.repository.EventoPlanoRepository;
import br.com.fiap.petbuddies.domain.repository.NotificacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificacaoSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoSchedulerService.class);

    private final EventoPlanoRepository eventoPlanoRepository;
    private final NotificacaoRepository notificacaoRepository;

    private volatile LocalDateTime ultimaExecucao;
    private volatile int totalCriadas = 0;

    public NotificacaoSchedulerService(EventoPlanoRepository eventoPlanoRepository,
                                        NotificacaoRepository notificacaoRepository) {
        this.eventoPlanoRepository = eventoPlanoRepository;
        this.notificacaoRepository = notificacaoRepository;
    }

    @Scheduled(fixedDelayString = "${scheduler.notificacao.delay:300000}")
    @Transactional
    public void processarNotificacoes() {
        ultimaExecucao = LocalDateTime.now();
        LocalDate hoje = LocalDate.now();
        LocalDate limite = hoje.plusDays(3);

        List<EventoPlanoEntity> eventos = eventoPlanoRepository
                .findByDataAlvoBetweenAndStatus(hoje, limite, StatusEventoPlano.PENDENTE);

        int criadas = 0;
        for (EventoPlanoEntity evento : eventos) {
            if (notificacaoRepository.existsByEventoPlanoId(evento.getId())) continue;

            NotificacaoEntity n = new NotificacaoEntity();
            n.setEventoPlano(evento);
            n.setPetNetApiAnimalId(evento.getPlano().getPetNetApiAnimalId());
            n.setCanal(CanalNotificacao.WHATSAPP);
            n.setStatusEnvio(StatusNotificacao.IGNORADA);
            n.setPayload("Lembrete: " + evento.getNome()
                    + " do plano de cuidados está programado para " + evento.getDataAlvo());
            notificacaoRepository.save(n);
            criadas++;
            log.info("[SCHEDULER] notificacao criada eventoId={} animalId={}",
                    evento.getId(), n.getPetNetApiAnimalId());
        }

        totalCriadas += criadas;
        if (criadas > 0) {
            log.info("[SCHEDULER] {} notificações criadas neste ciclo (total acumulado: {})",
                    criadas, totalCriadas);
        }
    }

    public LocalDateTime getUltimaExecucao() { return ultimaExecucao; }
    public int getTotalCriadas() { return totalCriadas; }
}
