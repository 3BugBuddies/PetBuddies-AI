package br.com.fiap.petbuddies.service;

import br.com.fiap.petbuddies.domain.entity.AnimalEntity;
import br.com.fiap.petbuddies.domain.entity.ItemPlanoCuidadoEntity;
import br.com.fiap.petbuddies.domain.entity.PlanoCuidadoEntity;
import br.com.fiap.petbuddies.domain.enums.CategoriaPlano;
import br.com.fiap.petbuddies.domain.enums.CategoriaProtocolo;
import br.com.fiap.petbuddies.domain.enums.Especie;
import br.com.fiap.petbuddies.domain.enums.MotivoSugestao;
import br.com.fiap.petbuddies.domain.enums.StatusItem;
import br.com.fiap.petbuddies.domain.enums.StatusPlano;
import br.com.fiap.petbuddies.domain.enums.TipoCuidado;
import br.com.fiap.petbuddies.domain.enums.TipoDataBase;
import br.com.fiap.petbuddies.domain.enums.TipoOrigemItem;
import br.com.fiap.petbuddies.domain.enums.UnidadeTempo;
import br.com.fiap.petbuddies.domain.repository.AnimalRepository;
import br.com.fiap.petbuddies.domain.repository.ItemPlanoCuidadoRepository;
import br.com.fiap.petbuddies.domain.repository.PlanoCuidadoRepository;
import br.com.fiap.petbuddies.dto.ItemPlanoCuidadoDto;
import br.com.fiap.petbuddies.dto.PlanoPreventivoRequest;
import br.com.fiap.petbuddies.dto.PlanoPosCirurgicoRequest;
import br.com.fiap.petbuddies.dto.PlanoResponse;
import br.com.fiap.petbuddies.dto.SugestaoCuidadoDto;
import br.com.fiap.petbuddies.exception.PlanoNaoEncontradoException;
import br.com.fiap.petbuddies.infrastructure.client.ProtocoloCatalogoDto;
import br.com.fiap.petbuddies.infrastructure.client.ProtocoloClient;
import br.com.fiap.petbuddies.infrastructure.client.RegraCatalogoDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MotorPlanoService {

    /** Ate onde a recorrencia e materializada. O plano nao tem fim; a tabela precisa ter. */
    private static final int HORIZONTE_MESES = 12;

    private final PlanoCuidadoRepository planoRepository;
    private final ItemPlanoCuidadoRepository itemRepository;
    private final ProtocoloClient protocoloClient;
    private final AnimalRepository animalRepository;

    public MotorPlanoService(PlanoCuidadoRepository planoRepository,
                             ItemPlanoCuidadoRepository itemRepository,
                             ProtocoloClient protocoloClient,
                             AnimalRepository animalRepository) {
        this.planoRepository = planoRepository;
        this.itemRepository = itemRepository;
        this.protocoloClient = protocoloClient;
        this.animalRepository = animalRepository;
    }

    /**
     * Sem {@code @Transactional} de proposito: entre a checagem de idempotencia
     * e a criacao ha uma chamada HTTP ao catalogo do .NET, e ela nao pode ficar
     * dentro de um bloco transacional — seguraria a conexao do pool pelo tempo
     * da rede. A checagem eager-carrega os itens (ver o @EntityGraph no
     * repositorio) e a criacao e um unico save() com cascade, cada uma com a
     * propria transacao curta.
     */
    public PlanoResponse instanciarPreventivo(PlanoPreventivoRequest req) {
        Optional<PlanoCuidadoEntity> existente = planoRepository
                .findPlanoAtivoPorCategoria(
                        req.getAnimalId(), StatusPlano.ATIVO, CategoriaPlano.PREVENTIVO);

        if (existente.isPresent()) {
            return PlanoResponse.from(existente.get(), false, "PLANO_JA_EXISTENTE");
        }

        Optional<ProtocoloCatalogoDto> protocolo = protocoloAplicavel(
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

    /** Ver o javadoc de {@link #instanciarPreventivo(PlanoPreventivoRequest)}. */
    public PlanoResponse instanciarPosCirurgico(PlanoPosCirurgicoRequest req) {
        Optional<PlanoCuidadoEntity> existente = planoRepository
                .findPlanoPorAnimalEConsulta(
                        req.getAnimalId(), req.getConsultaId());

        if (existente.isPresent()) {
            return PlanoResponse.from(existente.get(), false, "PLANO_JA_EXISTENTE");
        }

        Optional<ProtocoloCatalogoDto> protocolo = protocoloAplicavel(
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

    /**
     * O que o protocolo já produziu no animal: os planos com molde, e seus itens
     * separados em realizados, pendentes e vencidos. Leitura 100% local — nenhuma
     * chamada ao catálogo do .NET. É o que garante que a leitura de um plano já
     * materializado funciona inteira mesmo com o catálogo fora do ar.
     */
    @Transactional(readOnly = true)
    public List<PlanoCuidadoEntity> buscarProtocoloAplicado(Long animalId) {
        return planoRepository.findComProtocoloPorAnimal(animalId);
    }

    /**
     * Sugestão por histórico (PR-J9): reforço vencido é leitura local; recorrência
     * devida e nunca-realizado dependem do catálogo do .NET, e por isso somem
     * quando ele está fora do ar — a mesma degradação de {@link #protocoloAplicavel}.
     *
     * <p>Sem {@code @Transactional} de propósito, pelo mesmo motivo do javadoc de
     * {@link #instanciarPreventivo(PlanoPreventivoRequest)}: há uma chamada HTTP
     * no meio, entre duas leituras curtas de banco.</p>
     */
    public List<SugestaoCuidadoDto> sugerirPorHistorico(Long animalId) {
        List<SugestaoCuidadoDto> sugestoes = new ArrayList<>();

        itemRepository.findVencidosPorAnimal(animalId, TipoOrigemItem.PROTOCOLO,
                        List.of(StatusItem.PENDENTE, StatusItem.ATRASADO), LocalDate.now())
                .forEach(item -> sugestoes.add(SugestaoCuidadoDto.reforcoVencido(
                        animalId, item.getId(), item.getTipo(), item.getNome(), item.getDataAlvo())));

        sugestoes.addAll(sugerirPorRecorrencia(animalId));

        sugestoes.sort(Comparator.comparing(
                SugestaoCuidadoDto::getDataVencimento, Comparator.nullsLast(Comparator.naturalOrder())));
        return sugestoes;
    }

    /**
     * A metade que depende do catálogo: regras do protocolo preventivo ativo
     * ancoradas em {@code ULTIMA_REALIZACAO}. O pós-cirúrgico fica fora — pela
     * tabela do ADR s3-24 §2, ele é sempre âncora {@code DATA_CIRURGIA}, ocorrência
     * única, sem recorrência para sugerir.
     */
    private List<SugestaoCuidadoDto> sugerirPorRecorrencia(Long animalId) {
        Optional<PlanoCuidadoEntity> planoAtivo = planoRepository.findPlanoAtivoPorCategoria(
                animalId, StatusPlano.ATIVO, CategoriaPlano.PREVENTIVO);
        if (planoAtivo.isEmpty()) {
            return List.of();
        }

        Optional<AnimalEntity> animal = animalRepository.findById(animalId);
        if (animal.isEmpty()) {
            return List.of();
        }

        PlanoCuidadoEntity plano = planoAtivo.get();
        List<ProtocoloCatalogoDto> catalogo = protocoloClient.buscar(
                CategoriaProtocolo.valueOf(plano.getCategoria().name()), animal.get().getEspecie());

        Optional<ProtocoloCatalogoDto> protocoloAplicado = catalogo.stream()
                .filter(p -> p.id().equals(plano.getProtocoloId()))
                .findFirst();
        if (protocoloAplicado.isEmpty()) {
            return List.of(); // catalogo fora do ar, ou protocolo nao esta mais no ativo — nada a sugerir
        }

        List<RegraCatalogoDto> regrasPorHistorico = protocoloAplicado.get().regras().stream()
                .filter(r -> r.dataBase() == TipoDataBase.ULTIMA_REALIZACAO)
                .collect(Collectors.toList());
        if (regrasPorHistorico.isEmpty()) {
            return List.of();
        }

        List<TipoCuidado> tipos = regrasPorHistorico.stream()
                .map(RegraCatalogoDto::tipo)
                .distinct()
                .collect(Collectors.toList());
        List<ItemPlanoCuidadoEntity> historico = itemRepository.findHistoricoPorTipos(animalId, tipos);

        List<SugestaoCuidadoDto> sugestoes = new ArrayList<>();
        LocalDate hoje = LocalDate.now();
        for (RegraCatalogoDto regra : regrasPorHistorico) {
            List<ItemPlanoCuidadoEntity> itensDoTipo = historico.stream()
                    .filter(e -> e.getTipo() == regra.tipo())
                    .collect(Collectors.toList());

            boolean jaAgendado = itensDoTipo.stream()
                    .anyMatch(e -> e.getStatus() == StatusItem.PENDENTE
                            && e.getDataAlvo() != null && !e.getDataAlvo().isBefore(hoje));
            if (jaAgendado) {
                continue; // ja existe ocorrencia futura em aberto — nao duplica sugestao
            }

            Optional<ItemPlanoCuidadoEntity> ultimoRealizado = itensDoTipo.stream()
                    .filter(e -> e.getStatus() == StatusItem.REALIZADO)
                    .max(Comparator.comparing(MotorPlanoService::dataDeReferencia));

            if (ultimoRealizado.isEmpty()) {
                sugestoes.add(SugestaoCuidadoDto.porHistoricoDeTipo(
                        animalId, regra.tipo(), regra.nome(), null, MotivoSugestao.NUNCA_REALIZADO));
                continue;
            }

            LocalDate proxima = somar(dataDeReferencia(ultimoRealizado.get()), regra.offset(), regra.unidadeOffset());
            if (!proxima.isAfter(hoje)) {
                sugestoes.add(SugestaoCuidadoDto.porHistoricoDeTipo(
                        animalId, regra.tipo(), regra.nome(), proxima, MotivoSugestao.RECORRENCIA_DEVIDA));
            }
        }
        return sugestoes;
    }

    /** Data do "ultima vez que aconteceu": a execucao, quando existe, senao a data-alvo. */
    private static LocalDate dataDeReferencia(ItemPlanoCuidadoEntity item) {
        return item.getExecutadoEm() != null ? item.getExecutadoEm().toLocalDate() : item.getDataAlvo();
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
     * O protocolo ativo daquela categoria e especie, lido do catalogo do .NET
     * (ADR s3-25). Catalogo fora do ar devolve lista vazia — mesmo efeito de
     * "nenhum protocolo compativel".
     *
     * <p>Substitui o ProtocoloMatchService, removido no PR-J8. Ele escolhia o
     * "melhor match" por porte, sexo, castracao e faixa de idade — um automatismo
     * do tempo em que cadastrar o pet instanciava o plano sozinho (commit 295a543,
     * 19/05). As cinco colunas sairam do schema no ADR s3-24; sobrou ES_ESPECIE.</p>
     *
     * <p>Havendo mais de um candidato, o de menor id vence — determinismo, nao
     * criterio clinico. A escolha deliberada pelo veterinario e o PR-J9.</p>
     */
    private Optional<ProtocoloCatalogoDto> protocoloAplicavel(CategoriaProtocolo categoria,
                                                               Especie especie) {
        return protocoloClient.buscar(categoria, especie)
                .stream()
                .min(Comparator.comparing(ProtocoloCatalogoDto::id));
    }

    private PlanoCuidadoEntity criarPlano(Long animalId, Long consultaId,
                                                 ProtocoloCatalogoDto protocolo) {
        PlanoCuidadoEntity plano = new PlanoCuidadoEntity();
        plano.setAnimalId(animalId);
        plano.setConsultaId(consultaId);
        plano.setProtocoloId(protocolo.id());
        // A categoria e COPIADA do protocolo (ADR s3-24 §4b). E ela, e nao o join
        // com protocolo, que a consulta de idempotencia passa a ler — por isso um
        // plano de tratamento, que nao tem molde, deixa de ser invisivel para ela.
        plano.setCategoria(CategoriaPlano.valueOf(protocolo.categoria().name()));
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
    private void instanciarEventos(PlanoCuidadoEntity plano, ProtocoloCatalogoDto protocolo,
                                    Ancoragem ancoragem) {
        LocalDate limite = ancoragem.instanciacao().plusMonths(HORIZONTE_MESES);
        List<ItemPlanoCuidadoEntity> itens = new ArrayList<>();

        for (RegraCatalogoDto ep : protocolo.regras()) {
            LocalDate base = ancoragem.resolver(ep.dataBase());
            if (base == null) {
                // ancora sem data disponivel neste fluxo: o molde nao se aplica
                continue;
            }

            LocalDate primeira = somar(base, ep.offset(), ep.unidadeOffset());
            int repeticoes = ep.repeticoes() != null ? Math.max(ep.repeticoes(), 1) : 1;

            for (int i = 0; i < repeticoes; i++) {
                LocalDate alvo = primeira;
                if (i > 0) {
                    if (ep.intervalo() == null || ep.unidadeIntervalo() == null) {
                        break; // sem recorrencia declarada: ocorrencia unica
                    }
                    alvo = somar(primeira, ep.intervalo() * i, ep.unidadeIntervalo());
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

    private ItemPlanoCuidadoEntity novoItem(PlanoCuidadoEntity plano, RegraCatalogoDto ep,
                                        LocalDate dataAlvo) {
        ItemPlanoCuidadoEntity evento = new ItemPlanoCuidadoEntity();
        evento.setPlano(plano);
        evento.setRegraProtocoloId(ep.id());
        evento.setOrigem(TipoOrigemItem.PROTOCOLO);
        evento.setTipo(ep.tipo());
        evento.setNome(ep.nome());
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
