package br.com.fiap.petbuddies.service;

import br.com.fiap.petbuddies.domain.entity.ItemPlanoCuidadoEntity;
import br.com.fiap.petbuddies.domain.entity.RegraProtocoloEntity;
import br.com.fiap.petbuddies.domain.entity.PlanoCuidadoEntity;
import br.com.fiap.petbuddies.domain.entity.ProtocoloEntity;
import br.com.fiap.petbuddies.domain.enums.CategoriaPlano;
import br.com.fiap.petbuddies.domain.enums.CategoriaProtocolo;
import br.com.fiap.petbuddies.domain.enums.Especie;
import br.com.fiap.petbuddies.domain.enums.StatusItem;
import br.com.fiap.petbuddies.domain.enums.StatusPlano;
import br.com.fiap.petbuddies.domain.enums.TipoDataBase;
import br.com.fiap.petbuddies.domain.enums.TipoOrigemItem;
import br.com.fiap.petbuddies.domain.enums.UnidadeTempo;
import br.com.fiap.petbuddies.domain.repository.ItemPlanoCuidadoRepository;
import br.com.fiap.petbuddies.domain.repository.PlanoCuidadoRepository;
import br.com.fiap.petbuddies.domain.repository.ProtocoloRepository;
import br.com.fiap.petbuddies.dto.ItemPlanoCuidadoDto;
import br.com.fiap.petbuddies.dto.PlanoPreventivoRequest;
import br.com.fiap.petbuddies.dto.PlanoPosCirurgicoRequest;
import br.com.fiap.petbuddies.dto.PlanoResponse;
import br.com.fiap.petbuddies.exception.PlanoNaoEncontradoException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class MotorPlanoService {

    /** Ate onde a recorrencia e materializada. O plano nao tem fim; a tabela precisa ter. */
    private static final int HORIZONTE_MESES = 12;

    private final PlanoCuidadoRepository planoRepository;
    private final ItemPlanoCuidadoRepository itemRepository;
    private final ProtocoloRepository protocoloRepository;

    public MotorPlanoService(PlanoCuidadoRepository planoRepository,
                             ItemPlanoCuidadoRepository itemRepository,
                             ProtocoloRepository protocoloRepository) {
        this.planoRepository = planoRepository;
        this.itemRepository = itemRepository;
        this.protocoloRepository = protocoloRepository;
    }

    @Transactional
    public PlanoResponse instanciarPreventivo(PlanoPreventivoRequest req) {
        Optional<PlanoCuidadoEntity> existente = planoRepository
                .findPlanoAtivoPorCategoria(
                        req.getAnimalId(), StatusPlano.ATIVO, CategoriaPlano.PREVENTIVO);

        if (existente.isPresent()) {
            return PlanoResponse.from(existente.get(), false, "PLANO_JA_EXISTENTE");
        }

        Optional<ProtocoloEntity> protocolo = protocoloAplicavel(
                CategoriaProtocolo.PREVENTIVO, req.getEspecie());

        if (protocolo.isEmpty()) {
            return PlanoResponse.semProtocolo();
        }

        PlanoCuidadoEntity plano = criarPlano(req.getAnimalId(), null, protocolo.get());
        instanciarEventos(plano, protocolo.get(),
                new Ancoragem(LocalDate.now(), req.getDataNascimento(), null));
        planoRepository.save(plano);

        return PlanoResponse.from(plano, true, null);
    }

    @Transactional
    public PlanoResponse instanciarPosCirurgico(PlanoPosCirurgicoRequest req) {
        Optional<PlanoCuidadoEntity> existente = planoRepository
                .findPlanoPorAnimalEConsulta(
                        req.getAnimalId(), req.getConsultaId());

        if (existente.isPresent()) {
            return PlanoResponse.from(existente.get(), false, "PLANO_JA_EXISTENTE");
        }

        Optional<ProtocoloEntity> protocolo = protocoloAplicavel(
                CategoriaProtocolo.POS_CIRURGICO, req.getEspecie());

        if (protocolo.isEmpty()) {
            return PlanoResponse.semProtocolo();
        }

        LocalDate dataBase = req.getDataRealizacao().toLocalDate();
        PlanoCuidadoEntity plano = criarPlano(
                req.getAnimalId(), req.getConsultaId(), protocolo.get());
        instanciarEventos(plano, protocolo.get(), new Ancoragem(LocalDate.now(), null, dataBase));
        planoRepository.save(plano);

        return PlanoResponse.from(plano, true, null);
    }

    @Transactional(readOnly = true)
    public Optional<PlanoResponse> buscarPlanoAtivo(Long animalId) {
        return planoRepository
                .findPlanoAtivoPorCategoria(
                        animalId, StatusPlano.ATIVO, CategoriaPlano.PREVENTIVO)
                .map(PlanoResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<ItemPlanoCuidadoDto> listarEventos(Long animalId, Pageable pageable) {
        return itemRepository
                .findEventosPorAnimal(animalId, pageable)
                .map(ItemPlanoCuidadoDto::from);
    }

    @Transactional
    public void cancelarPlano(Long planoId, String motivo) {
        PlanoCuidadoEntity plano = planoRepository.findById(planoId)
                .orElseThrow(() -> new PlanoNaoEncontradoException(planoId));
        // decisao I: DT_CANCELADO_EM e MT_MOTIVO_CANCELAMENTO sairam do schema da S3.
        // O motivo continua no contrato do endpoint, mas nao e persistido.
        plano.setStatus(StatusPlano.CANCELADO);
        plano.getItens().stream()
                .filter(e -> e.getStatus() == StatusItem.PENDENTE)
                .forEach(e -> e.setStatus(StatusItem.CANCELADO));
        planoRepository.save(plano);
    }

    /**
     * O protocolo ativo daquela categoria e especie.
     *
     * <p>Substitui o ProtocoloMatchService, removido no PR-J8. Ele escolhia o
     * "melhor match" por porte, sexo, castracao e faixa de idade — um automatismo
     * do tempo em que cadastrar o pet instanciava o plano sozinho (commit 295a543,
     * 19/05). As cinco colunas sairam do schema no ADR s3-24; sobrou ES_ESPECIE.</p>
     *
     * <p>Havendo mais de um candidato, o de menor id vence — determinismo, nao
     * criterio clinico. A escolha deliberada pelo veterinario e o PR-J9.</p>
     */
    private Optional<ProtocoloEntity> protocoloAplicavel(CategoriaProtocolo categoria,
                                                         Especie especie) {
        return protocoloRepository
                .findByCategoriaAndEspecieAndAtivoTrue(categoria, especie)
                .stream()
                .min(Comparator.comparing(ProtocoloEntity::getId));
    }

    private PlanoCuidadoEntity criarPlano(Long animalId, Long consultaId,
                                                 ProtocoloEntity protocolo) {
        PlanoCuidadoEntity plano = new PlanoCuidadoEntity();
        plano.setAnimalId(animalId);
        plano.setConsultaId(consultaId);
        plano.setProtocolo(protocolo);
        // A categoria e COPIADA do protocolo (ADR s3-24 §4b). E ela, e nao o join
        // com protocolo, que a consulta de idempotencia passa a ler — por isso um
        // plano de tratamento, que nao tem molde, deixa de ser invisivel para ela.
        plano.setCategoria(CategoriaPlano.valueOf(protocolo.getCategoria().name()));
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
    private void instanciarEventos(PlanoCuidadoEntity plano, ProtocoloEntity protocolo,
                                    Ancoragem ancoragem) {
        LocalDate limite = ancoragem.instanciacao().plusMonths(HORIZONTE_MESES);
        List<ItemPlanoCuidadoEntity> itens = new ArrayList<>();

        for (RegraProtocoloEntity ep : protocolo.getRegras()) {
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

        itens.sort(Comparator.comparing(ItemPlanoCuidadoEntity::getDataAlvo));
        plano.getItens().addAll(itens);
    }

    private ItemPlanoCuidadoEntity novoItem(PlanoCuidadoEntity plano, RegraProtocoloEntity ep,
                                        LocalDate dataAlvo) {
        ItemPlanoCuidadoEntity evento = new ItemPlanoCuidadoEntity();
        evento.setPlano(plano);
        evento.setRegraProtocolo(ep);
        evento.setOrigem(TipoOrigemItem.PROTOCOLO);
        evento.setTipo(ep.getTipo());
        evento.setNome(ep.getNome());
        evento.setDataAlvo(dataAlvo);
        evento.setStatus(StatusItem.PENDENTE);
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
     * As datas-base disponiveis no fluxo que esta instanciando o plano. Uma
     * data-base sem valor correspondente faz a regra ser ignorada, em vez de gerar
     * item numa data inventada.
     *
     * <p>ULTIMA_REALIZACAO devolve nulo aqui de proposito: resolve-la exige
     * consultar o que o animal ja fez, e isso e o PR-J9. Ate la, uma regra
     * ancorada nela simplesmente nao produz item — falha visivel, nao silenciosa.</p>
     */
    private record Ancoragem(LocalDate instanciacao, LocalDate nascimento, LocalDate dataCirurgia) {
        LocalDate resolver(TipoDataBase dataBase) {
            if (dataBase == null) {
                return instanciacao;
            }
            return switch (dataBase) {
                case NASCIMENTO -> nascimento;
                case DATA_CIRURGIA -> dataCirurgia;
                case ULTIMA_REALIZACAO -> null;
            };
        }
    }
}
