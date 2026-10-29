package br.com.fiap.petbuddies.service.cuidado;

import br.com.fiap.petbuddies.domain.entity.PlanoCuidadoEntity;
import br.com.fiap.petbuddies.domain.repository.PlanoCuidadoRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PlanoCuidadoGravador {

    private final PlanoCuidadoRepository planoRepository;

    public PlanoCuidadoGravador(PlanoCuidadoRepository planoRepository) {
        this.planoRepository = planoRepository;
    }

    // REQUIRES_NEW: no listener AFTER_COMMIT a transacao do cadastro ja fechou, e um save que a reaproveitasse nao seria commitado.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PlanoCuidadoEntity gravar(PlanoCuidadoEntity plano) {
        return planoRepository.save(plano);
    }
}
