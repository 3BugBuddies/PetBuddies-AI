package br.com.fiap.petbuddies.service.prescricao;

import br.com.fiap.petbuddies.domain.entity.PrescricaoEntity;
import br.com.fiap.petbuddies.domain.repository.PrescricaoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Prescrições cuja janela assinada — [DT_INICIO, DT_INICIO + NR_DURACAO_DIAS) —
 * cobre a data de referência. A prescrição não tem coluna de status; a
 * vigência é sempre derivada da faixa de dias.
 *
 * <p>Compartilhado por {@link CheckinExtracaoService} (monta o vocabulário do
 * prompt) e {@link CheckinService} (decide quais prescrições avaliar), para
 * as duas nunca divergirem sobre o que está "ativo" no mesmo check-in.</p>
 */
@Service
public class PrescricaoAtivaResolver {

    private final PrescricaoRepository repository;

    public PrescricaoAtivaResolver(PrescricaoRepository repository) {
        this.repository = repository;
    }

    public List<PrescricaoEntity> listar(Long animalId, LocalDate referencia) {
        return repository.findByAnimalIdOrderByDataInicioDesc(animalId).stream()
                .filter(p -> vigente(p, referencia))
                .toList();
    }

    private boolean vigente(PrescricaoEntity prescricao, LocalDate referencia) {
        LocalDate fim = prescricao.getDataInicio().plusDays(prescricao.getDuracaoDias());
        return !referencia.isBefore(prescricao.getDataInicio()) && referencia.isBefore(fim);
    }
}
