package br.com.fiap.petbuddies.service;

import br.com.fiap.petbuddies.domain.entity.AnimalEntity;
import br.com.fiap.petbuddies.domain.entity.CheckinEntity;
import br.com.fiap.petbuddies.domain.entity.CondicaoClinicaEntity;
import br.com.fiap.petbuddies.domain.entity.CondicaoObservadaEntity;
import br.com.fiap.petbuddies.domain.entity.ItemPlanoCuidadoEntity;
import br.com.fiap.petbuddies.domain.entity.PrescricaoEntity;
import br.com.fiap.petbuddies.domain.entity.RegraPrescricaoEntity;
import br.com.fiap.petbuddies.domain.enums.TipoDado;
import br.com.fiap.petbuddies.domain.repository.AnimalRepository;
import br.com.fiap.petbuddies.domain.repository.CheckinRepository;
import br.com.fiap.petbuddies.domain.repository.CondicaoClinicaRepository;
import br.com.fiap.petbuddies.domain.repository.CondicaoObservadaRepository;
import br.com.fiap.petbuddies.domain.repository.ItemPlanoCuidadoRepository;
import br.com.fiap.petbuddies.domain.repository.PrescricaoRepository;
import br.com.fiap.petbuddies.domain.repository.RegraPrescricaoRepository;
import br.com.fiap.petbuddies.dto.CheckinDesfechoResponse;
import br.com.fiap.petbuddies.dto.CheckinRequest;
import br.com.fiap.petbuddies.dto.CheckinResponse;
import br.com.fiap.petbuddies.dto.CondicaoConfirmadaRequest;
import br.com.fiap.petbuddies.dto.CondicaoObservadaResponse;
import br.com.fiap.petbuddies.exception.AnimalNaoEncontradoException;
import br.com.fiap.petbuddies.exception.CheckinDuplicadoException;
import br.com.fiap.petbuddies.exception.CheckinNaoEncontradoException;
import br.com.fiap.petbuddies.exception.CondicaoClinicaNaoEncontradaException;
import br.com.fiap.petbuddies.exception.CondicaoObservadaIncoerenteException;
import br.com.fiap.petbuddies.exception.ItemPlanoCuidadoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * O caso de uso do check-in (§5.1 do documento de IA, passo F em diante):
 * recebe a narrativa já confirmada pelo tutor, grava check-in e condições
 * observadas, avalia a regra congelada de cada prescrição afetada, e grava o
 * desfecho no item do plano.
 *
 * <p>Sem chamada ao modelo aqui — isso já aconteceu em
 * {@link CheckinExtracaoService}. Este serviço é determinístico do início ao
 * fim, o que é o que torna o ciclo auditável (guardrail 5 e 8).</p>
 */
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

    public CheckinService(
            CheckinRepository checkinRepository,
            CondicaoObservadaRepository condicaoObservadaRepository,
            AnimalRepository animalRepository,
            CondicaoClinicaRepository condicaoClinicaRepository,
            ItemPlanoCuidadoRepository itemPlanoCuidadoRepository,
            PrescricaoRepository prescricaoRepository,
            RegraPrescricaoRepository regraPrescricaoRepository,
            PrescricaoAtivaResolver prescricaoAtivaResolver,
            AvaliadorRegraService avaliadorRegraService) {
        this.checkinRepository = checkinRepository;
        this.condicaoObservadaRepository = condicaoObservadaRepository;
        this.animalRepository = animalRepository;
        this.condicaoClinicaRepository = condicaoClinicaRepository;
        this.itemPlanoCuidadoRepository = itemPlanoCuidadoRepository;
        this.prescricaoRepository = prescricaoRepository;
        this.regraPrescricaoRepository = regraPrescricaoRepository;
        this.prescricaoAtivaResolver = prescricaoAtivaResolver;
        this.avaliadorRegraService = avaliadorRegraService;
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
        }

        if (checkinRepository.findExistente(animal.getId(), referencia, request.getItemPlanoCuidadoId()).isPresent()) {
            throw new CheckinDuplicadoException(animal.getId(), referencia, request.getItemPlanoCuidadoId());
        }

        CheckinEntity checkin = new CheckinEntity();
        checkin.setAnimal(animal);
        checkin.setItemPlanoCuidado(itemRelatado);
        checkin.setDataReferencia(referencia);
        checkin.setRegistradoEm(LocalDateTime.now());
        checkin.setNarrativa(request.getNarrativa());
        checkin.setTicUtilizada(request.getTicUtilizada());
        checkin = checkinRepository.save(checkin);

        List<CondicaoObservadaEntity> observadas = gravarCondicoes(checkin, request.getCondicoes());
        boolean escalarPorCritica = observadas.stream().anyMatch(this::observacaoCritica);

        List<PrescricaoEntity> prescricoesParaAvaliar = itemRelatado != null
                ? prescricaoDoItem(itemRelatado)
                : prescricaoAtivaResolver.listar(animal.getId(), referencia);

        List<CheckinDesfechoResponse> desfechos = new ArrayList<>();
        for (PrescricaoEntity prescricao : prescricoesParaAvaliar) {
            List<RegraPrescricaoEntity> regras = regraPrescricaoRepository.findByPrescricaoIdOrderByOrdemAsc(prescricao.getId());
            AvaliadorRegraService.Resultado resultado =
                    avaliadorRegraService.avaliar(prescricao, regras, observadas, escalarPorCritica);

            ItemPlanoCuidadoEntity item = itemParaGravar(itemRelatado, prescricao, referencia);
            if (item != null) {
                item.setCheckinId(checkin.getId());
                item.setDesfecho(resultado.desfecho());
                item.setDoseAplicada(resultado.doseAplicada());
                item.setRegraAplicadaId(resultado.regraAplicadaId());
                itemPlanoCuidadoRepository.save(item);
            }

            desfechos.add(CheckinDesfechoResponse.of(
                    prescricao.getId(), prescricao.getMedicamento(), item == null ? null : item.getId(),
                    resultado.desfecho(), resultado.doseAplicada(), prescricao.getUnidade(), resultado.regraAplicadaId()));
        }

        if (escalarPorCritica) {
            checkin.setObservacoesGerais(montarObservacaoEscalacao(observadas, prescricoesParaAvaliar));
            checkin = checkinRepository.save(checkin);
        }

        List<CondicaoObservadaResponse> condicoesResponse = observadas.stream().map(CondicaoObservadaResponse::from).toList();
        return CheckinResponse.of(checkin, condicoesResponse, desfechos, escalarPorCritica);
    }

    private CheckinEntity encontrarOuFalhar(Long id) {
        return checkinRepository.findById(id).orElseThrow(() -> new CheckinNaoEncontradoException(id));
    }

    /** Item já existe para a prescrição avaliada, e é onde a baixa é gravada. Não instancia item novo (fora da fronteira deste PR). */
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
            entity.setValorBooleano(confirmada.getValorBooleano());
            entity.setValorNumerico(confirmada.getValorNumerico());
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

    // Guardrail 6 (ADR s3-16): sem limiar no catálogo para condição NUMERICO
    // crítica, então qualquer valor relatado (não nulo) já escala — a
    // alternativa seria o código julgando gravidade, que é o que o ADR evita.
    private boolean observacaoCritica(CondicaoObservadaEntity observada) {
        if (!observada.getCondicaoClinica().isCritica()) {
            return false;
        }
        if (observada.getValorBooleano() != null) {
            return observada.getValorBooleano();
        }
        return observada.getValorNumerico() != null;
    }

    private String montarObservacaoEscalacao(List<CondicaoObservadaEntity> observadas, List<PrescricaoEntity> prescricoes) {
        String rotulos = observadas.stream()
                .filter(this::observacaoCritica)
                .map(o -> o.getCondicaoClinica().getRotulo())
                .distinct()
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        String telefone = prescricoes.stream()
                .map(p -> p.getVeterinario().getClinica().getTelefone())
                .findFirst()
                .orElse(null);
        String base = "Sinal de atenção identificado (" + rotulos + "). Procure a clínica";
        return telefone == null ? base + "." : base + ": " + telefone + ".";
    }

    private CheckinResponse montarResposta(CheckinEntity checkin) {
        List<CondicaoObservadaEntity> observadas = condicaoObservadaRepository.findByCheckin_Id(checkin.getId());
        List<CondicaoObservadaResponse> condicoes = observadas.stream().map(CondicaoObservadaResponse::from).toList();

        // Só reconstitui o que foi de fato gravado no item — prescrição
        // avaliada sem item na hora do POST não deixa rastro para o GET.
        List<CheckinDesfechoResponse> desfechos = itemPlanoCuidadoRepository.findByCheckinId(checkin.getId()).stream()
                .map(item -> {
                    PrescricaoEntity prescricao = item.getPrescricaoId() == null
                            ? null : prescricaoRepository.findById(item.getPrescricaoId()).orElse(null);
                    return CheckinDesfechoResponse.of(
                            item.getPrescricaoId(), prescricao == null ? null : prescricao.getMedicamento(),
                            item.getId(), item.getDesfecho(), item.getDoseAplicada(),
                            prescricao == null ? null : prescricao.getUnidade(), item.getRegraAplicadaId());
                })
                .toList();

        // Mesmo critério do POST (escalarPorCritica): condição crítica
        // observada, não "algum item terminou ACIONAR_CLINICA" — a
        // escalação vale mesmo quando não existe item pra gravar.
        boolean escalado = observadas.stream().anyMatch(this::observacaoCritica);
        return CheckinResponse.of(checkin, condicoes, desfechos, escalado);
    }
}
