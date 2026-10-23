package br.com.fiap.petbuddies.service.checkin;

import br.com.fiap.petbuddies.domain.embeddable.Desfecho;
import br.com.fiap.petbuddies.domain.entity.CheckinEntity;
import br.com.fiap.petbuddies.domain.entity.CondicaoObservadaEntity;
import br.com.fiap.petbuddies.domain.entity.ItemPlanoCuidadoEntity;
import br.com.fiap.petbuddies.domain.entity.PrescricaoEntity;
import br.com.fiap.petbuddies.domain.entity.RegraPrescricaoEntity;
import br.com.fiap.petbuddies.domain.enums.checkin.TipoDesfecho;
import br.com.fiap.petbuddies.domain.repository.CheckinRepository;
import br.com.fiap.petbuddies.domain.repository.CondicaoObservadaRepository;
import br.com.fiap.petbuddies.domain.repository.ItemPlanoCuidadoRepository;
import br.com.fiap.petbuddies.domain.repository.PrescricaoRepository;
import br.com.fiap.petbuddies.domain.repository.RegraPrescricaoRepository;
import br.com.fiap.petbuddies.dto.checkin.RelatoParaLerDto;
import br.com.fiap.petbuddies.dto.checkin.RelatoParaLerDto.Tipo;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Leitura dos check-ins para o início do painel. Não grava nada. */
@Service
public class RelatoPainelService {

    private final CheckinRepository checkinRepository;
    private final ItemPlanoCuidadoRepository itemRepository;
    private final CondicaoObservadaRepository condicaoObservadaRepository;
    private final RegraPrescricaoRepository regraRepository;
    private final PrescricaoRepository prescricaoRepository;

    public RelatoPainelService(
            CheckinRepository checkinRepository,
            ItemPlanoCuidadoRepository itemRepository,
            CondicaoObservadaRepository condicaoObservadaRepository,
            RegraPrescricaoRepository regraRepository,
            PrescricaoRepository prescricaoRepository) {
        this.checkinRepository = checkinRepository;
        this.itemRepository = itemRepository;
        this.condicaoObservadaRepository = condicaoObservadaRepository;
        this.regraRepository = regraRepository;
        this.prescricaoRepository = prescricaoRepository;
    }

    /**
     * Check-ins da janela, com o alarme primeiro, depois "sem regra", depois dose; dentro de cada grupo
     * o mais recente primeiro. Alarme é desfecho ACIONAR_CLINICA ou condição crítica relatada; "sem regra"
     * é o relato em que nenhuma regra da receita agiu.
     */
    @Transactional(readOnly = true)
    public List<RelatoParaLerDto> paraLer(Duration janela) {
        List<CheckinEntity> checkins = checkinRepository.findDesdeComAnimal(LocalDateTime.now().minus(janela));
        if (checkins.isEmpty()) {
            return List.of();
        }
        List<Long> ids = checkins.stream().map(CheckinEntity::getId).toList();

        Map<Long, List<ItemPlanoCuidadoEntity>> itensPorCheckin = itemRepository.findByDesfechoCheckinIdIn(ids).stream()
                .filter(i -> i.getDesfecho() != null)
                .collect(Collectors.groupingBy(i -> i.getDesfecho().getCheckinId()));
        Map<Long, List<CondicaoObservadaEntity>> observadasPorCheckin =
                condicaoObservadaRepository.findComCondicaoPorCheckins(ids).stream()
                        .collect(Collectors.groupingBy(o -> o.getCheckin().getId()));

        List<Long> regraIds = itensPorCheckin.values().stream().flatMap(List::stream)
                .map(i -> i.getDesfecho().getRegraAplicadaId()).filter(Objects::nonNull).distinct().toList();
        Map<Long, RegraPrescricaoEntity> regras = regraIds.isEmpty() ? Map.of()
                : regraRepository.findAllById(regraIds).stream()
                        .collect(Collectors.toMap(RegraPrescricaoEntity::getId, r -> r));
        List<Long> prescricaoIds = itensPorCheckin.values().stream().flatMap(List::stream)
                .map(ItemPlanoCuidadoEntity::getPrescricaoId).filter(Objects::nonNull).distinct().toList();
        Map<Long, String> unidades = prescricaoIds.isEmpty() ? Map.of()
                : prescricaoRepository.findAllById(prescricaoIds).stream()
                        .collect(Collectors.toMap(PrescricaoEntity::getId, p -> p.getFaixaDose().getUnidade()));

        return checkins.stream()
                .map(c -> montar(c, itensPorCheckin.getOrDefault(c.getId(), List.of()),
                        observadasPorCheckin.getOrDefault(c.getId(), List.of()), regras, unidades))
                .sorted(Comparator.comparing(RelatoParaLerDto::tipo)
                        .thenComparing(RelatoParaLerDto::registradoEm, Comparator.reverseOrder()))
                .toList();
    }

    /** Check-ins registrados no dia, do mais cedo ao mais tarde. */
    @Transactional(readOnly = true)
    public List<RelatoParaLerDto> doDia(LocalDate dia) {
        return checkinRepository.findDesdeComAnimal(dia.atStartOfDay()).stream()
                .filter(c -> c.getRegistradoEm().toLocalDate().equals(dia))
                .sorted(Comparator.comparing(CheckinEntity::getRegistradoEm))
                .map(c -> new RelatoParaLerDto(c.getId(), c.getAnimal().getId(), c.getAnimal().getNome(),
                        c.getAnimal().getResponsavel().getNome(), c.getRegistradoEm(), c.getNarrativa(), null, null))
                .toList();
    }

    private RelatoParaLerDto montar(
            CheckinEntity checkin,
            List<ItemPlanoCuidadoEntity> itens,
            List<CondicaoObservadaEntity> observadas,
            Map<Long, RegraPrescricaoEntity> regras,
            Map<Long, String> unidades) {
        List<String> criticas = observadas.stream().filter(this::critica)
                .map(o -> o.getCondicaoClinica().getRotulo()).distinct().toList();
        boolean alarme = !criticas.isEmpty()
                || itens.stream().anyMatch(i -> i.getDesfecho().getTipo() == TipoDesfecho.ACIONAR_CLINICA);
        boolean regraAgiu = itens.stream().anyMatch(i -> i.getDesfecho().getRegraAplicadaId() != null);

        Tipo tipo = alarme ? Tipo.ALARME : (regraAgiu ? Tipo.DOSE : Tipo.SEM_REGRA);
        List<String> frases = itens.stream()
                // Map.of() lança NPE em get(null): alarme por sinal crítico não tem regra aplicada.
                .map(i -> frase(i,
                        i.getDesfecho().getRegraAplicadaId() == null ? null : regras.get(i.getDesfecho().getRegraAplicadaId()),
                        i.getPrescricaoId() == null ? null : unidades.get(i.getPrescricaoId())))
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
        if (alarme && frases.stream().noneMatch(f -> f.endsWith("acionar clínica"))) {
            frases.add(0, (criticas.isEmpty() ? "" : String.join(", ", criticas) + " → ") + "acionar clínica");
        }
        String resumo = frases.isEmpty() ? "Nenhuma regra cobre; a dose não mudou." : String.join(" · ", frases);

        return new RelatoParaLerDto(checkin.getId(), checkin.getAnimal().getId(), checkin.getAnimal().getNome(),
                checkin.getAnimal().getResponsavel().getNome(), checkin.getRegistradoEm(), checkin.getNarrativa(),
                tipo, resumo);
    }

    private String frase(ItemPlanoCuidadoEntity item, RegraPrescricaoEntity regra, String unidade) {
        Desfecho desfecho = item.getDesfecho();
        if (regra == null) {
            return null;
        }
        String acao = switch (regra.getAcaoDose()) {
            case DOSE_MIN -> "dose mínima" + dose(desfecho, unidade);
            case DOSE_MAX -> "dose máxima" + dose(desfecho, unidade);
            case DOSE_PADRAO -> "dose padrão" + dose(desfecho, unidade);
            case ACIONAR_CLINICA -> "acionar clínica";
            case SUSPENDER -> "suspender";
        };
        return regra.getCondicaoCongelada().getRotulo() + " → " + acao;
    }

    private String dose(Desfecho desfecho, String unidade) {
        if (desfecho.getDoseAplicada() == null) {
            return "";
        }
        return " (" + desfecho.getDoseAplicada().stripTrailingZeros().toPlainString()
                + (unidade == null ? "" : " " + unidade) + ")";
    }

    // Mesmo critério de CheckinService.observacaoCritica: condição crítica com valor relatado.
    private boolean critica(CondicaoObservadaEntity observada) {
        if (!observada.getCondicaoClinica().isCritica()) {
            return false;
        }
        if (observada.getValor().getBooleano() != null) {
            return observada.getValor().getBooleano();
        }
        return observada.getValor().getNumerico() != null;
    }
}
