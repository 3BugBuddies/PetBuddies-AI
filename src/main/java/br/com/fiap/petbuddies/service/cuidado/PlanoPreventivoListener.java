package br.com.fiap.petbuddies.service.cuidado;

import br.com.fiap.petbuddies.domain.event.AnimalCadastrado;
import br.com.fiap.petbuddies.dto.cuidado.PlanoPreventivoRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PlanoPreventivoListener {

    private static final Logger log = LoggerFactory.getLogger(PlanoPreventivoListener.class);

    private final MotorPlanoService motorPlanoService;

    public PlanoPreventivoListener(MotorPlanoService motorPlanoService) {
        this.motorPlanoService = motorPlanoService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoCadastrarAnimal(AnimalCadastrado evento) {
        try {
            motorPlanoService.instanciarPreventivo(PlanoPreventivoRequest.from(evento.animal()));
        } catch (Exception e) {
            // O pet ja foi commitado: falha do catalogo nao pode virar 500 do POST.
            log.warn("[PLANO-PREVENTIVO] animalId={} nasceu sem plano preventivo", evento.animal().getId(), e);
        }
    }
}
