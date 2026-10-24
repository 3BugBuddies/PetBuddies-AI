package br.com.fiap.petbuddies.service.checkin;

import br.com.fiap.petbuddies.domain.embeddable.Desfecho;
import br.com.fiap.petbuddies.domain.embeddable.ValorObservado;
import br.com.fiap.petbuddies.domain.entity.AnimalEntity;
import br.com.fiap.petbuddies.domain.entity.CheckinEntity;
import br.com.fiap.petbuddies.domain.entity.CondicaoClinicaEntity;
import br.com.fiap.petbuddies.domain.entity.CondicaoObservadaEntity;
import br.com.fiap.petbuddies.domain.entity.ItemPlanoCuidadoEntity;
import br.com.fiap.petbuddies.domain.entity.PlanoCuidadoEntity;
import br.com.fiap.petbuddies.domain.entity.PrescricaoEntity;
import br.com.fiap.petbuddies.domain.entity.RegraPrescricaoEntity;
import br.com.fiap.petbuddies.domain.enums.checkin.TipoDesfecho;
import br.com.fiap.petbuddies.domain.enums.cuidado.CategoriaPlano;
import br.com.fiap.petbuddies.domain.enums.cuidado.StatusItem;
import br.com.fiap.petbuddies.domain.enums.cuidado.StatusPlano;
import br.com.fiap.petbuddies.domain.enums.cuidado.TipoOrigemItem;
import br.com.fiap.petbuddies.domain.enums.prescricao.TipoDado;
import br.com.fiap.petbuddies.domain.repository.AnimalRepository;
import br.com.fiap.petbuddies.domain.repository.CheckinRepository;
import br.com.fiap.petbuddies.domain.repository.CondicaoClinicaRepository;
import br.com.fiap.petbuddies.domain.repository.CondicaoObservadaRepository;
import br.com.fiap.petbuddies.domain.repository.ItemPlanoCuidadoRepository;
import br.com.fiap.petbuddies.domain.repository.PrescricaoRepository;
import br.com.fiap.petbuddies.domain.repository.RegraPrescricaoRepository;
import br.com.fiap.petbuddies.dto.checkin.CheckinDesfechoResponse;
import br.com.fiap.petbuddies.dto.checkin.CheckinRequest;
import br.com.fiap.petbuddies.dto.checkin.CheckinResponse;
import br.com.fiap.petbuddies.dto.checkin.CondicaoConfirmadaRequest;
import br.com.fiap.petbuddies.dto.checkin.CondicaoObservadaResponse;
import br.com.fiap.petbuddies.exception.atendimento.CondicaoClinicaNaoEncontradaException;
import br.com.fiap.petbuddies.exception.cadastro.AnimalNaoEncontradoException;
import br.com.fiap.petbuddies.exception.checkin.CheckinDuplicadoException;
import br.com.fiap.petbuddies.exception.checkin.CheckinNaoEncontradoException;
import br.com.fiap.petbuddies.exception.checkin.CondicaoObservadaIncoerenteException;
import br.com.fiap.petbuddies.exception.checkin.ItemDeOutroAnimalException;
import br.com.fiap.petbuddies.exception.cuidado.ItemPlanoCuidadoNaoEncontradoException;
import br.com.fiap.petbuddies.service.cadastro.ContatoClinicaService;
import br.com.fiap.petbuddies.service.prescricao.PrescricaoAtivaResolver;
import br.com.fiap.petbuddies.web.Formatador;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

// Sem chamada ao modelo aqui — ja aconteceu em CheckinExtracaoService; deterministico do inicio ao fim.
@Service
public class CheckinService {

    private final CheckinRepository checkinRepository;
    private final CondicaoObservadaRepository condicaoObservadaRepository;
    private final AnimalRepository animalRepository;
    private final CondicaoClinicaRepository condicaoClinicaRepository;
    private final ItemPlanoCuidadoRepository itemPlanoCuidadoRepository;
    private final PrescricaoRepository prescricaoRepository;
    private final RegraPrescricaoRepository regraPrescricaoRepository;
    private final PrescricaoAtivaResolver prescricaoAtivaResolver;
    private final AvaliadorRegraService avaliadorRegraService;
    private final Formatador formatador;
    private final ContatoClinicaService contatoClinicaService;

    public CheckinService(
            CheckinRepository checkinRepository,
            CondicaoObservadaRepository condicaoObservadaRepository,
            AnimalRepository animalRepository,
            CondicaoClinicaRepository condicaoClinicaRepository,
            ItemPlanoCuidadoRepository itemPlanoCuidadoRepository,
            PrescricaoRepository prescricaoRepository,
            RegraPrescricaoRepository regraPrescricaoRepository,
            PrescricaoAtivaResolver prescricaoAtivaResolver,
            AvaliadorRegraService avaliadorRegraService,
            Formatador formatador,
            ContatoClinicaService contatoClinicaService) {
        this.checkinRepository = checkinRepository;
        this.condicaoObservadaRepository = condicaoObservadaRepository;
        this.animalRepository = animalRepository;
        this.condicaoClinicaRepository = condicaoClinicaRepository;
        this.itemPlanoCuidadoRepository = itemPlanoCuidadoRepository;
        this.prescricaoRepository = prescricaoRepository;
        this.regraPrescricaoRepository = regraPrescricaoRepository;
        this.prescricaoAtivaResolver = prescricaoAtivaResolver;
        this.avaliadorRegraService = avaliadorRegraService;
        this.formatador = formatador;
        this.contatoClinicaService = contatoClinicaService;
    }

    @Transactional(readOnly = true)
    public List<CheckinResponse> listar(Long animalId) {
        return checkinRepository.findByAnimal_IdOrderByDataReferenciaDesc(animalId).stream()
                .map(this::montarResposta)
                .toList();
    }

    @Transactional(readOnly = true)
    public CheckinResponse buscarPorId(Long id) {
        return montarResposta(encontrarOuFalhar(id));
    }

    @Transactional
    public CheckinResponse registrar(CheckinRequest request) {
        LocalDate referencia = request.getDataReferencia() == null ? LocalDate.now() : request.getDataReferencia();
        AnimalEntity animal = animalRepository.findById(request.getAnimalId())
                .orElseThrow(() -> new AnimalNaoEncontradoException(request.getAnimalId()));

        ItemPlanoCuidadoEntity itemRelatado = null;
        if (request.getItemPlanoCuidadoId() != null) {
            itemRelatado = itemPlanoCuidadoRepository.findById(request.getItemPlanoCuidadoId())
                    .orElseThrow(() -> new ItemPlanoCuidadoNaoEncontradoException(request.getItemPlanoCuidadoId()));
            if (!itemRelatado.getPlano().getAnimalId().equals(animal.getId())) {
                throw new ItemDeOutroAnimalException(itemRelatado.getId(), animal.getId());
            }
        }

        if (checkinRepository.findExistente(animal.getId(), referencia, request.getItemPlanoCuidadoId()).isPresent()) {
            throw new CheckinDuplicadoException(animal.getId(), referencia, request.getItemPlanoCuidadoId());
        }
        // Relato geral e relato por item no mesmo dia avaliariam a mesma prescrição duas vezes.
        if (itemRelatado != null && checkinRepository.existsByAnimal_IdAndDataReferenciaAndItemPlanoCuidadoIsNull(animal.getId(), referencia)) {
            throw new CheckinDuplicadoException(animal.getId(), referencia, "Já existe relato geral neste dia; o relato por item não é aceito.");
        }
        if (itemRelatado == null && checkinRepository.existsByAnimal_IdAndDataReferenciaAndItemPlanoCuidadoIsNotNull(animal.getId(), referencia)) {
            throw new CheckinDuplicadoException(animal.getId(), referencia, "Já existe relato por item neste dia; o relato geral não é aceito.");
        }

        CheckinEntity checkin = new CheckinEntity();
        checkin.setAnimal(animal);
        checkin.setItemPlanoCuidado(itemRelatado);
        checkin.setDataReferencia(referencia);
        checkin.setRegistradoEm(LocalDateTime.now());
        checkin.setNarrativa(request.getNarrativa());
        checkin.setTicUtilizada(request.getTicUtilizada());
        checkin = checkinRepository.save(checkin);

        // "condicoes": null explicito no JSON sobrescreve o default da lista vazia do DTO.
        List<CondicaoConfirmadaRequest> condicoes = request.getCondicoes() == null ? List.of() : request.getCondicoes();
        List<CondicaoObservadaEntity> observadas = gravarCondicoes(checkin, condicoes);
        boolean escalarPorCritica = observadas.stream().anyMatch(this::observacaoCritica);

        List<PrescricaoEntity> prescricoesParaAvaliar = itemRelatado != null
                ? prescricaoDoItem(itemRelatado)
                : prescricaoAtivaResolver.listar(animal.getId(), referencia);

        List<CheckinDesfechoResponse> desfechos = new ArrayList<>();
        // A suspensão roda depois do laço: suspender no meio faria o plano parecer SUSPENSO para as prescrições seguintes do mesmo relato.
        Map<PrescricaoEntity, ItemPlanoCuidadoEntity> aSuspender = new java.util.LinkedHashMap<>();
        for (PrescricaoEntity prescricao : prescricoesParaAvaliar) {
            List<RegraPrescricaoEntity> regras = regraPrescricaoRepository.findByPrescricaoIdOrderByOrdemAsc(prescricao.getId());
            AvaliadorRegraService.Resultado resultado =
                    avaliadorRegraService.avaliar(prescricao, regras, observadas, escalarPorCritica);

            ItemPlanoCuidadoEntity item = itemParaGravar(itemRelatado, prescricao, referencia);
            if (item != null) {
                item.setDesfecho(new Desfecho(checkin.getId(), resultado.desfecho(), resultado.doseAplicada(), resultado.regraAplicadaId()));
                // Plano suspenso pelo alarme: o desfecho é registrado, mas o item não volta para REALIZADO.
                if (item.getOrigem() == TipoOrigemItem.PRESCRICAO && item.getPlano().getStatus() != StatusPlano.SUSPENSO) {
                    item.setStatus(statusPorDesfecho(resultado.desfecho()));
                    item.setExecutadoEm(checkin.getRegistradoEm());
                }
                itemPlanoCuidadoRepository.save(item);
            }
            if (resultado.desfecho() == TipoDesfecho.ACIONAR_CLINICA) {
                aSuspender.put(prescricao, item);
            }

            desfechos.add(CheckinDesfechoResponse.of(
                    prescricao.getId(), prescricao.getMedicamento(), item == null ? null : item.getId(),
                    resultado.desfecho(), resultado.doseAplicada(), prescricao.getFaixaDose().getUnidade(), resultado.regraAplicadaId(),
                    prescricao).comRegra(regras.stream()
                            .filter(r -> r.getId().equals(resultado.regraAplicadaId())).findFirst().orElse(null)));
        }

        aSuspender.forEach((prescricao, item) -> suspenderTratamento(prescricao, item, referencia));

        if (escalarPorCritica) {
            checkin.setObservacoesGerais(montarObservacaoEscalacao(observadas, prescricoesParaAvaliar));
            checkin = checkinRepository.save(checkin);
        }

        List<CondicaoObservadaResponse> condicoesResponse = observadas.stream().map(CondicaoObservadaResponse::from).toList();
        return CheckinResponse.of(checkin, condicoesResponse, desfechos, escalarPorCritica,
                contatoClinicaService.doAnimal(animal.getId(), prescricoesParaAvaliar));
    }

    // Sinal de alarme suspende o tratamento até a veterinária reavaliar; o preventivo segue.
    private void suspenderTratamento(PrescricaoEntity prescricao, ItemPlanoCuidadoEntity item, LocalDate referencia) {
        ItemPlanoCuidadoEntity referenciaDoPlano = item != null ? item
                : itemPlanoCuidadoRepository.findFirstByPrescricaoIdOrderByDataAlvoAsc(prescricao.getId()).orElse(null);
        if (referenciaDoPlano == null) {
            return;
        }
        PlanoCuidadoEntity plano = referenciaDoPlano.getPlano();
        if (plano.getCategoria() != CategoriaPlano.TRATAMENTO || plano.getStatus() != StatusPlano.ATIVO) {
            return;
        }
        plano.setStatus(StatusPlano.SUSPENSO);
        plano.getItens().stream()
                .filter(i -> i.getStatus() == StatusItem.PENDENTE && i.getDataAlvo() != null && i.getDataAlvo().isAfter(referencia))
                .forEach(i -> i.setStatus(StatusItem.SUSPENSO));
    }


    private StatusItem statusPorDesfecho(TipoDesfecho desfecho) {
        return switch (desfecho) {
            case DOSE_CALCULADA -> StatusItem.REALIZADO;
            case SEM_DOSE -> StatusItem.SUSPENSO;
            case ACIONAR_CLINICA -> StatusItem.ENCAMINHADO;
        };
    }

    private CheckinEntity encontrarOuFalhar(Long id) {
        return checkinRepository.findById(id).orElseThrow(() -> new CheckinNaoEncontradoException(id));
    }

    // Item ja existe para a prescricao avaliada — nao instancia item novo.
    private ItemPlanoCuidadoEntity itemParaGravar(ItemPlanoCuidadoEntity itemRelatado, PrescricaoEntity prescricao, LocalDate referencia) {
        if (itemRelatado != null && prescricao.getId().equals(itemRelatado.getPrescricaoId())) {
            return itemRelatado;
        }
        return itemPlanoCuidadoRepository.findByPrescricaoIdAndDataAlvo(prescricao.getId(), referencia).orElse(null);
    }

    private List<PrescricaoEntity> prescricaoDoItem(ItemPlanoCuidadoEntity item) {
        if (item.getPrescricaoId() == null) {
            return List.of(); // item de origem PROTOCOLO não tem prescrição para avaliar
        }
        return prescricaoRepository.findById(item.getPrescricaoId()).map(List::of).orElse(List.of());
    }

    private List<CondicaoObservadaEntity> gravarCondicoes(CheckinEntity checkin, List<CondicaoConfirmadaRequest> confirmadas) {
        List<CondicaoObservadaEntity> observadas = new ArrayList<>();
        for (CondicaoConfirmadaRequest confirmada : confirmadas) {
            CondicaoClinicaEntity condicao = condicaoClinicaRepository.findById(confirmada.getCondicaoClinicaId())
                    .orElseThrow(() -> new CondicaoClinicaNaoEncontradaException(confirmada.getCondicaoClinicaId()));
            validarCoerencia(condicao.getTipoDado(), confirmada.getValorBooleano(), confirmada.getValorNumerico());

            CondicaoObservadaEntity entity = new CondicaoObservadaEntity();
            entity.setCheckin(checkin);
            entity.setCondicaoClinica(condicao);
            entity.setCodigoCongelado(condicao.getCodigo());
            entity.setValor(new ValorObservado(confirmada.getValorBooleano(), confirmada.getValorNumerico()));
            entity.setConfianca(confirmada.getConfianca().setScale(4, RoundingMode.HALF_UP));
            observadas.add(condicaoObservadaRepository.save(entity));
        }
        return observadas;
    }

    // O @AssertTrue do DTO já garante CK_COBS_UM_VALOR; falta só confirmar que
    // o valor preenchido bate com o TP_DADO da condição — o DTO não sabe disso.
    private void validarCoerencia(TipoDado tipoDado, Boolean valorBooleano, BigDecimal valorNumerico) {
        boolean numerico = tipoDado == TipoDado.NUMERICO;
        if (numerico && valorNumerico == null) {
            throw new CondicaoObservadaIncoerenteException("Condição NUMERICO exige valorNumerico preenchido.");
        }
        if (!numerico && valorBooleano == null) {
            throw new CondicaoObservadaIncoerenteException("Condição BOOLEANO exige valorBooleano preenchido.");
        }
    }

    // Sem limiar no catalogo para NUMERICO critica — qualquer valor relatado ja escala; alternativa seria o codigo julgando gravidade.
    private boolean observacaoCritica(CondicaoObservadaEntity observada) {
        if (!observada.getCondicaoClinica().isCritica()) {
            return false;
        }
        if (observada.getValor().getBooleano() != null) {
            return observada.getValor().getBooleano();
        }
        return observada.getValor().getNumerico() != null;
    }

    private String montarObservacaoEscalacao(List<CondicaoObservadaEntity> observadas, List<PrescricaoEntity> prescricoes) {
        String rotulos = observadas.stream()
                .filter(this::observacaoCritica)
                .map(o -> o.getCondicaoClinica().getRotulo())
                .distinct()
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        String telefone = prescricoes.stream()
                .map(p -> p.getVeterinario().getClinica().getContato().getTelefone())
                .findFirst()
                .orElse(null);
        String base = "Sinal de atenção identificado (" + rotulos + "). Procure a clínica";
        return telefone == null ? base + "." : base + ": " + formatador.telefone(telefone) + ".";
    }

    private CheckinResponse montarResposta(CheckinEntity checkin) {
        List<CondicaoObservadaEntity> observadas = condicaoObservadaRepository.findByCheckin_Id(checkin.getId());
        List<CondicaoObservadaResponse> condicoes = observadas.stream().map(CondicaoObservadaResponse::from).toList();

        // Só reconstitui o que foi de fato gravado no item — prescrição
        // avaliada sem item na hora do POST não deixa rastro para o GET.
        List<ItemPlanoCuidadoEntity> itens = itemPlanoCuidadoRepository.findByDesfechoCheckinId(checkin.getId());
        List<Long> prescricaoIds = itens.stream().map(ItemPlanoCuidadoEntity::getPrescricaoId).filter(Objects::nonNull).distinct().toList();
        Map<Long, PrescricaoEntity> prescricoes = prescricaoIds.isEmpty() ? Map.of()
                : prescricaoRepository.findComPrescritorPorIds(prescricaoIds).stream()
                        .collect(Collectors.toMap(PrescricaoEntity::getId, p -> p));
        List<CheckinDesfechoResponse> desfechos = itens.stream()
                .map(item -> {
                    PrescricaoEntity prescricao = item.getPrescricaoId() == null ? null : prescricoes.get(item.getPrescricaoId());
                    // Com todas as colunas nulas, o Hibernate devolve o Desfecho inteiro como null.
                    Desfecho desfecho = item.getDesfecho();
                    return CheckinDesfechoResponse.of(
                            item.getPrescricaoId(), prescricao == null ? null : prescricao.getMedicamento(),
                            item.getId(), desfecho == null ? null : desfecho.getTipo(),
                            desfecho == null ? null : desfecho.getDoseAplicada(),
                            prescricao == null ? null : prescricao.getFaixaDose().getUnidade(),
                            desfecho == null ? null : desfecho.getRegraAplicadaId(),
                            prescricao).comRegra(desfecho == null || desfecho.getRegraAplicadaId() == null ? null
                            : regraPrescricaoRepository.findById(desfecho.getRegraAplicadaId()).orElse(null));
                })
                .toList();

        // Mesmo critério do POST (escalarPorCritica): condição crítica
        // observada, não "algum item terminou ACIONAR_CLINICA" — a
        // escalação vale mesmo quando não existe item pra gravar.
        boolean escalado = observadas.stream().anyMatch(this::observacaoCritica);
        return CheckinResponse.of(checkin, condicoes, desfechos, escalado,
                contatoClinicaService.doAnimal(checkin.getAnimal().getId(), prescricoes.values()));
    }
}
