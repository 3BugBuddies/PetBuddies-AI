package br.com.fiap.petbuddies.service.prescricao;

import br.com.fiap.petbuddies.domain.entity.PrescricaoEntity;
import br.com.fiap.petbuddies.domain.enums.cuidado.StatusPlano;
import br.com.fiap.petbuddies.domain.repository.ItemPlanoCuidadoRepository;
import br.com.fiap.petbuddies.domain.repository.PrescricaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final ItemPlanoCuidadoRepository itemRepository;

    public PrescricaoAtivaResolver(PrescricaoRepository repository, ItemPlanoCuidadoRepository itemRepository) {
        this.repository = repository;
        this.itemRepository = itemRepository;
    }

    // O check-in de extração roda sem transação (chama a IA); sem esta, o plano do item é proxy desanexado.
    @Transactional(readOnly = true)
    public List<PrescricaoEntity> listar(Long animalId, LocalDate referencia) {
        return repository.findByAnimalIdOrderByDataInicioDesc(animalId).stream()
                .filter(p -> vigente(p, referencia))
                .toList();
    }

    private boolean vigente(PrescricaoEntity prescricao, LocalDate referencia) {
        if (prescricao.getDataInicio() == null || prescricao.getDuracaoDias() == null) {
            return false;
        }
        LocalDate fim = prescricao.getDataInicio().plusDays(prescricao.getDuracaoDias());
        boolean naJanela = !referencia.isBefore(prescricao.getDataInicio()) && referencia.isBefore(fim);
        // Plano suspenso pelo alarme não tem dose a avaliar até a veterinária reabrir o tratamento.
        return naJanela && itemRepository.findFirstByPrescricaoIdOrderByDataAlvoAsc(prescricao.getId())
                .map(i -> i.getPlano().getStatus() != StatusPlano.SUSPENSO)
                .orElse(true);
    }
}
