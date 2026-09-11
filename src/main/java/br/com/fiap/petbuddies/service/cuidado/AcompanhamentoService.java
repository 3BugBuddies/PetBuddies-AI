package br.com.fiap.petbuddies.service.cuidado;

import br.com.fiap.petbuddies.domain.entity.AnimalEntity;
import br.com.fiap.petbuddies.domain.entity.ItemPlanoCuidadoEntity;
import br.com.fiap.petbuddies.domain.entity.PlanoCuidadoEntity;
import br.com.fiap.petbuddies.domain.enums.cuidado.StatusItem;
import br.com.fiap.petbuddies.domain.enums.cuidado.StatusPlano;
import br.com.fiap.petbuddies.domain.repository.AnimalRepository;
import br.com.fiap.petbuddies.domain.repository.ItemPlanoCuidadoRepository;
import br.com.fiap.petbuddies.domain.repository.PlanoCuidadoRepository;
import br.com.fiap.petbuddies.dto.cuidado.ItemVencidoDto;
import br.com.fiap.petbuddies.dto.cuidado.ResumoAcompanhamento;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Leitura agregada do cuidado para o painel da clínica: quantos planos estão
 * de pé, o que passou da data e quanto a clínica está conseguindo cumprir.
 */
@Service
public class AcompanhamentoService {

    /** Janela da adesão, em dias. O rótulo da tela vem daqui. */
    private static final int JANELA_ADESAO_DIAS = 30;

    private final PlanoCuidadoRepository planoRepository;
    private final ItemPlanoCuidadoRepository itemRepository;
    private final AnimalRepository animalRepository;

    public AcompanhamentoService(
            PlanoCuidadoRepository planoRepository,
            ItemPlanoCuidadoRepository itemRepository,
            AnimalRepository animalRepository) {
        this.planoRepository = planoRepository;
        this.itemRepository = itemRepository;
        this.animalRepository = animalRepository;
    }

    @Transactional(readOnly = true)
    public ResumoAcompanhamento resumo() {
        LocalDate hoje = LocalDate.now();
        LocalDate desde = hoje.minusDays(JANELA_ADESAO_DIAS);

        List<PlanoCuidadoEntity> ativos = planoRepository.findByStatus(StatusPlano.ATIVO);
        List<ItemPlanoCuidadoEntity> vencidos = buscarVencidos(hoje);

        long vencidosNaJanela = vencidos.stream()
                .filter(i -> !i.getDataAlvo().isBefore(desde))
                .count();
        long realizadosNaJanela =
                itemRepository.contarPorStatusDesde(StatusPlano.ATIVO, StatusItem.REALIZADO, desde);
        long previstosNaJanela = realizadosNaJanela + vencidosNaJanela;

        // Sem nenhum cuidado previsto na janela, uma adesão de 0% ou de 100% mentiria
        // do mesmo jeito. O nulo é o que a tela lê como "ainda não dá para dizer".
        Integer adesao = previstosNaJanela == 0
                ? null
                : (int) Math.round(realizadosNaJanela * 100.0 / previstosNaJanela);

        return new ResumoAcompanhamento(
                ativos.size(),
                ativos.stream().map(PlanoCuidadoEntity::getAnimalId).distinct().count(),
                vencidos.size(),
                vencidos.stream().map(i -> i.getPlano().getAnimalId()).distinct().count(),
                adesao,
                realizadosNaJanela,
                previstosNaJanela,
                JANELA_ADESAO_DIAS);
    }

    /** Os vencidos do mais atrasado para o menos, já com nome de animal e de tutor. */
    @Transactional(readOnly = true)
    public List<ItemVencidoDto> vencidos(int limite) {
        LocalDate hoje = LocalDate.now();
        List<ItemPlanoCuidadoEntity> itens = buscarVencidos(hoje);
        if (itens.isEmpty()) {
            return List.of();
        }

        Set<Long> animaisIds = itens.stream()
                .map(i -> i.getPlano().getAnimalId())
                .collect(Collectors.toSet());
        Map<Long, AnimalEntity> animais = animalRepository.findComResponsavelPorIds(animaisIds).stream()
                .collect(Collectors.toMap(AnimalEntity::getId, Function.identity()));

        return itens.stream()
                .map(item -> {
                    AnimalEntity animal = animais.get(item.getPlano().getAnimalId());
                    return new ItemVencidoDto(
                            item.getPlano().getAnimalId(),
                            animal == null ? "—" : animal.getNome(),
                            animal == null ? "—" : animal.getResponsavel().getNome(),
                            item.getNome(),
                            item.getDataAlvo(),
                            ChronoUnit.DAYS.between(item.getDataAlvo(), hoje));
                })
                .sorted(Comparator.comparingLong(ItemVencidoDto::diasAtraso).reversed())
                .limit(limite)
                .toList();
    }

    private List<ItemPlanoCuidadoEntity> buscarVencidos(LocalDate hoje) {
        return itemRepository.findVencidosDaClinica(
                StatusPlano.ATIVO, StatusItem.ATRASADO, StatusItem.PENDENTE, hoje);
    }
}
