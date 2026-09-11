package br.com.fiap.petbuddies.service.cuidado;

import br.com.fiap.petbuddies.domain.entity.ItemPlanoCuidadoEntity;
import br.com.fiap.petbuddies.domain.entity.PlanoCuidadoEntity;
import br.com.fiap.petbuddies.domain.entity.PrescricaoEntity;
import br.com.fiap.petbuddies.domain.enums.cuidado.CategoriaPlano;
import br.com.fiap.petbuddies.domain.enums.cuidado.StatusItem;
import br.com.fiap.petbuddies.domain.enums.cuidado.StatusPlano;
import br.com.fiap.petbuddies.domain.enums.cuidado.TipoCuidado;
import br.com.fiap.petbuddies.domain.enums.cuidado.TipoOrigemItem;
import br.com.fiap.petbuddies.domain.repository.ItemPlanoCuidadoRepository;
import br.com.fiap.petbuddies.domain.repository.PlanoCuidadoRepository;
import br.com.fiap.petbuddies.exception.cuidado.DuracaoTratamentoExcedeTetoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Nasce de prescricao assinada, nao de molde do catalogo — mesma forma de MotorPlanoService.criarPlano/instanciarEventos, sem protocolo nem catalogo do .NET.
@Service
public class PlanoTratamentoService {

    // Uso continuo nao declara fim; um valor absurdo no formulario nao pode materializar milhares de linhas na mesma transacao.
    private static final int TETO_DURACAO_DIAS = 365;

    private final PlanoCuidadoRepository planoRepository;
    private final ItemPlanoCuidadoRepository itemRepository;

    public PlanoTratamentoService(PlanoCuidadoRepository planoRepository,
                                   ItemPlanoCuidadoRepository itemRepository) {
        this.planoRepository = planoRepository;
        this.itemRepository = itemRepository;
    }

    /** O plano ATIVO de categoria TRATAMENTO do animal, criando um se não houver. */
    public PlanoCuidadoEntity garantirPlano(Long animalId) {
        Optional<PlanoCuidadoEntity> existente = planoRepository
                .findPlanoAtivoPorCategoria(animalId, StatusPlano.ATIVO, CategoriaPlano.TRATAMENTO);
        return existente.orElseGet(() -> planoRepository.save(criarPlano(animalId)));
    }

    // Sem dataInicio ou duracaoDias, a prescricao e de uso continuo — sem itens, sem erro.
    // Reprocessar nao duplica: mesma consulta que o check-in usa para achar o item ja gravado.
    @Transactional
    public void materializarItens(PrescricaoEntity prescricao) {
        LocalDate dataInicio = prescricao.getDataInicio();
        Integer duracaoDias = prescricao.getDuracaoDias();
        if (dataInicio == null || duracaoDias == null) {
            return;
        }
        if (duracaoDias > TETO_DURACAO_DIAS) {
            throw new DuracaoTratamentoExcedeTetoException(duracaoDias, TETO_DURACAO_DIAS);
        }

        PlanoCuidadoEntity plano = garantirPlano(prescricao.getAnimal().getId());

        List<ItemPlanoCuidadoEntity> novos = new ArrayList<>();
        for (int i = 0; i < duracaoDias; i++) {
            LocalDate dataAlvo = dataInicio.plusDays(i);
            if (itemRepository.findByPrescricaoIdAndDataAlvo(prescricao.getId(), dataAlvo).isPresent()) {
                continue;
            }
            novos.add(novoItem(plano, prescricao, dataAlvo));
        }
        plano.getItens().addAll(novos);
        planoRepository.save(plano);
    }

    private PlanoCuidadoEntity criarPlano(Long animalId) {
        PlanoCuidadoEntity plano = new PlanoCuidadoEntity();
        plano.setAnimalId(animalId);
        plano.setCategoria(CategoriaPlano.TRATAMENTO);
        plano.setStatus(StatusPlano.ATIVO);
        return plano;
    }

    private ItemPlanoCuidadoEntity novoItem(PlanoCuidadoEntity plano, PrescricaoEntity prescricao, LocalDate dataAlvo) {
        ItemPlanoCuidadoEntity item = new ItemPlanoCuidadoEntity();
        item.setPlano(plano);
        item.setOrigem(TipoOrigemItem.PRESCRICAO);
        item.setPrescricaoId(prescricao.getId());
        item.setTipo(TipoCuidado.MEDICACAO);
        item.setNome(prescricao.getMedicamento());
        item.setDataAlvo(dataAlvo);
        item.setStatus(StatusItem.PENDENTE);
        return item;
    }
}
