package br.com.fiap.petbuddies.service;

import br.com.fiap.petbuddies.domain.entity.EventoPlanoEntity;
import br.com.fiap.petbuddies.domain.entity.EventoProtocoloEntity;
import br.com.fiap.petbuddies.domain.entity.PlanoCuidadoAnimalEntity;
import br.com.fiap.petbuddies.domain.entity.ProtocoloEntity;
import br.com.fiap.petbuddies.domain.enums.CategoriaProtocolo;
import br.com.fiap.petbuddies.domain.enums.StatusEventoPlano;
import br.com.fiap.petbuddies.domain.enums.StatusPlano;
import br.com.fiap.petbuddies.domain.enums.TipoAncora;
import br.com.fiap.petbuddies.domain.enums.TipoOrigemItem;
import br.com.fiap.petbuddies.domain.enums.UnidadeTempo;
import br.com.fiap.petbuddies.domain.repository.EventoPlanoRepository;
import br.com.fiap.petbuddies.domain.repository.PlanoCuidadoAnimalRepository;
import br.com.fiap.petbuddies.dto.EventoPlanoDto;
import br.com.fiap.petbuddies.dto.PlanoPreventivoRequest;
import br.com.fiap.petbuddies.dto.PlanoPosCirurgicoRequest;
import br.com.fiap.petbuddies.dto.PlanoResponse;
import br.com.fiap.petbuddies.exception.PlanoNaoEncontradoException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class MotorPlanoService {

    /** Ate onde a recorrencia e materializada. O plano nao tem fim; a tabela precisa ter. */
    private static final int HORIZONTE_MESES = 12;

    private final PlanoCuidadoAnimalRepository planoRepository;
    private final EventoPlanoRepository eventoPlanoRepository;
    private final ProtocoloMatchService protocoloMatchService;

    public MotorPlanoService(PlanoCuidadoAnimalRepository planoRepository,
                             EventoPlanoRepository eventoPlanoRepository,
                             ProtocoloMatchService protocoloMatchService) {
        this.planoRepository = planoRepository;
        this.eventoPlanoRepository = eventoPlanoRepository;
        this.protocoloMatchService = protocoloMatchService;
    }

    @Transactional
    public PlanoResponse instanciarPreventivo(PlanoPreventivoRequest req) {
        Optional<PlanoCuidadoAnimalEntity> existente = planoRepository
                .findPlanoAtivoPorCategoria(
                        req.getAnimalId(), StatusPlano.ATIVO, CategoriaProtocolo.PREVENTIVO);

        if (existente.isPresent()) {
            return PlanoResponse.from(existente.get(), false, "PLANO_JA_EXISTENTE");
        }

        int idadeEmMeses = (int) ChronoUnit.MONTHS.between(req.getDataNascimento(), LocalDate.now());

        Optional<ProtocoloEntity> protocolo = protocoloMatchService.encontrarMelhorMatch(
                CategoriaProtocolo.PREVENTIVO,
                req.getEspecie(), req.getPorte(), req.getSexo(), req.getCastrado(), idadeEmMeses);

        if (protocolo.isEmpty()) {
            return PlanoResponse.semProtocolo();
        }

        PlanoCuidadoAnimalEntity plano = criarPlano(req.getAnimalId(), null, protocolo.get());
        instanciarEventos(plano, protocolo.get(),
                new Ancoragem(LocalDate.now(), req.getDataNascimento(), null));
        planoRepository.save(plano);

        return PlanoResponse.from(plano, true, null);
    }

    @Transactional
    public PlanoResponse instanciarPosCirurgico(PlanoPosCirurgicoRequest req) {
        Optional<PlanoCuidadoAnimalEntity> existente = planoRepository
                .findPlanoPorAnimalEConsulta(
                        req.getAnimalId(), req.getConsultaId());

        if (existente.isPresent()) {
            return PlanoResponse.from(existente.get(), false, "PLANO_JA_EXISTENTE");
        }

        Optional<ProtocoloEntity> protocolo = protocoloMatchService.encontrarMelhorMatch(
                CategoriaProtocolo.POS_CIRURGICO, req.getEspecie(), null, null, null, 0);

        if (protocolo.isEmpty()) {
            return PlanoResponse.semProtocolo();
        }

        LocalDate dataBase = req.getDataRealizacao().toLocalDate();
        PlanoCuidadoAnimalEntity plano = criarPlano(
                req.getAnimalId(), req.getConsultaId(), protocolo.get());
        instanciarEventos(plano, protocolo.get(), new Ancoragem(LocalDate.now(), null, dataBase));
        planoRepository.save(plano);

        return PlanoResponse.from(plano, true, null);
    }

    @Transactional(readOnly = true)
    public Optional<PlanoResponse> buscarPlanoAtivo(Long animalId) {
        return planoRepository
                .findPlanoAtivoPorCategoria(
                        animalId, StatusPlano.ATIVO, CategoriaProtocolo.PREVENTIVO)
                .map(PlanoResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<EventoPlanoDto> listarEventos(Long animalId, Pageable pageable) {
        return eventoPlanoRepository
                .findEventosPorAnimal(animalId, pageable)
                .map(EventoPlanoDto::from);
    }

    @Transactional
    public void cancelarPlano(Long planoId, String motivo) {
        PlanoCuidadoAnimalEntity plano = planoRepository.findById(planoId)
                .orElseThrow(() -> new PlanoNaoEncontradoException(planoId));
        // decisao I: DT_CANCELADO_EM e MT_MOTIVO_CANCELAMENTO sairam do schema da S3.
        // O motivo continua no contrato do endpoint, mas nao e persistido.
        plano.setStatus(StatusPlano.CANCELADO);
        plano.getEventos().stream()
                .filter(e -> e.getStatus() == StatusEventoPlano.PENDENTE)
                .forEach(e -> e.setStatus(StatusEventoPlano.CANCELADO));
        planoRepository.save(plano);
    }

    private PlanoCuidadoAnimalEntity criarPlano(Long animalId, Long consultaId,
                                                 ProtocoloEntity protocolo) {
        PlanoCuidadoAnimalEntity plano = new PlanoCuidadoAnimalEntity();
        plano.setAnimalId(animalId);
        plano.setConsultaId(consultaId);
        plano.setProtocolo(protocolo);
        plano.setStatus(StatusPlano.ATIVO);
        return plano;
    }

    /**
     * Expande cada molde do protocolo nos itens concretos do plano.
     *
     * <p>Para cada evento: resolve a ancora, soma o deslocamento na unidade
     * declarada, e repete o item conforme intervalo e numero de repeticoes. Itens
     * alem do horizonte de {@value #HORIZONTE_MESES} meses sao descartados — a
     * recorrencia declara "para sempre" com um numero, mas o plano nao tem fim, e
     * materializar tudo encheria a tabela sem ninguem ler.</p>
     *
     * <p>A ordenacao e feita aqui, sobre a data ja resolvida: deslocamento 2 em
     * MESES e 10 em DIAS nao sao comparaveis antes disso.</p>
     */
    private void instanciarEventos(PlanoCuidadoAnimalEntity plano, ProtocoloEntity protocolo,
                                    Ancoragem ancoragem) {
        LocalDate limite = ancoragem.instanciacao().plusMonths(HORIZONTE_MESES);
        List<EventoPlanoEntity> itens = new ArrayList<>();

        for (EventoProtocoloEntity ep : protocolo.getEventos()) {
            LocalDate base = ancoragem.resolver(ep.getAncora());
            if (base == null) {
                // ancora sem data disponivel neste fluxo: o molde nao se aplica
                continue;
            }

            LocalDate primeira = somar(base, ep.getOffset(), ep.getUnidadeOffset());
            int repeticoes = ep.getRepeticoes() != null ? Math.max(ep.getRepeticoes(), 1) : 1;

            for (int i = 0; i < repeticoes; i++) {
                LocalDate alvo = primeira;
                if (i > 0) {
                    if (ep.getIntervalo() == null || ep.getUnidadeIntervalo() == null) {
                        break; // sem recorrencia declarada: ocorrencia unica
                    }
                    alvo = somar(primeira, ep.getIntervalo() * i, ep.getUnidadeIntervalo());
                }
                if (alvo.isAfter(limite)) {
                    break;
                }
                itens.add(novoItem(plano, ep, alvo));
            }
        }

        itens.sort(Comparator.comparing(EventoPlanoEntity::getDataAlvo));
        plano.getEventos().addAll(itens);
    }

    private EventoPlanoEntity novoItem(PlanoCuidadoAnimalEntity plano, EventoProtocoloEntity ep,
                                        LocalDate dataAlvo) {
        EventoPlanoEntity evento = new EventoPlanoEntity();
        evento.setPlano(plano);
        evento.setEventoProtocolo(ep);
        evento.setOrigem(TipoOrigemItem.PROTOCOLO);
        evento.setTipo(ep.getTipo());
        evento.setNome(ep.getNome());
        evento.setDataAlvo(dataAlvo);
        evento.setStatus(StatusEventoPlano.PENDENTE);
        return evento;
    }

    private static LocalDate somar(LocalDate base, int quantidade, UnidadeTempo unidade) {
        return switch (unidade) {
            case DIAS -> base.plusDays(quantidade);
            case SEMANAS -> base.plusWeeks(quantidade);
            case MESES -> base.plusMonths(quantidade);
        };
    }

    /**
     * As datas-base disponiveis no fluxo que esta instanciando o plano. Uma ancora
     * sem data correspondente faz o molde ser ignorado, em vez de gerar item numa
     * data inventada.
     */
    private record Ancoragem(LocalDate instanciacao, LocalDate nascimento, LocalDate dataCirurgia) {
        LocalDate resolver(TipoAncora ancora) {
            if (ancora == null) {
                return instanciacao;
            }
            return switch (ancora) {
                case INSTANCIACAO -> instanciacao;
                case NASCIMENTO -> nascimento;
                case DATA_CIRURGIA -> dataCirurgia;
            };
        }
    }
}
