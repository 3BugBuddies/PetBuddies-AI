package br.com.fiap.petbuddies.service.cuidado;

import br.com.fiap.petbuddies.domain.entity.AnimalEntity;
import br.com.fiap.petbuddies.domain.entity.ItemPlanoCuidadoEntity;
import br.com.fiap.petbuddies.domain.entity.PlanoCuidadoEntity;
import br.com.fiap.petbuddies.domain.enums.cuidado.CategoriaPlano;
import br.com.fiap.petbuddies.domain.enums.cuidado.CategoriaProtocolo;
import br.com.fiap.petbuddies.domain.enums.cadastro.Especie;
import br.com.fiap.petbuddies.domain.enums.cuidado.MotivoSugestao;
import br.com.fiap.petbuddies.domain.enums.cuidado.StatusItem;
import br.com.fiap.petbuddies.domain.enums.cuidado.StatusPlano;
import br.com.fiap.petbuddies.domain.enums.cuidado.TipoCuidado;
import br.com.fiap.petbuddies.domain.enums.cuidado.TipoDataBase;
import br.com.fiap.petbuddies.domain.enums.cuidado.TipoOrigemItem;
import br.com.fiap.petbuddies.domain.enums.cuidado.UnidadeTempo;
import br.com.fiap.petbuddies.domain.repository.AnimalRepository;
import br.com.fiap.petbuddies.domain.repository.ItemPlanoCuidadoRepository;
import br.com.fiap.petbuddies.domain.repository.PlanoCuidadoRepository;
import br.com.fiap.petbuddies.dto.cuidado.ItemPlanoCuidadoDto;
import br.com.fiap.petbuddies.dto.cuidado.PlanoPreventivoRequest;
import br.com.fiap.petbuddies.dto.cuidado.PlanoPosCirurgicoRequest;
import br.com.fiap.petbuddies.dto.cuidado.PlanoResponse;
import br.com.fiap.petbuddies.dto.cuidado.SugestaoCuidadoDto;
import br.com.fiap.petbuddies.exception.cuidado.PlanoNaoEncontradoException;
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

    // Sem @Transactional de proposito: ha chamada HTTP ao catalogo entre a checagem e a criacao — nao pode segurar conexao do pool durante a rede.
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

    // Sem @Transactional de proposito, pelo mesmo motivo do instanciarPreventivo.
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
        for (CategoriaPlano categoria : List.of(
                CategoriaPlano.PREVENTIVO, CategoriaPlano.POS_CIRURGICO, CategoriaPlano.TRATAMENTO)) {
            Optional<PlanoCuidadoEntity> plano = planoRepository
                    .findPlanoAtivoPorCategoria(animalId, StatusPlano.ATIVO, categoria);
            if (plano.isPresent()) {
                return plano.map(PlanoResponse::from);
            }
        }
        return Optional.empty();
    }

    @Transactional(readOnly = true)
    public Page<ItemPlanoCuidadoDto> listarEventos(Long animalId, Pageable pageable) {
        return itemRepository
                .findEventosPorAnimal(animalId, pageable)
                .map(ItemPlanoCuidadoDto::from);
    }

    // Leitura 100% local — sem chamada ao catalogo do .NET.
    @Transactional(readOnly = true)
    public List<PlanoCuidadoEntity> buscarProtocoloAplicado(Long animalId) {
        return planoRepository.findComProtocoloPorAnimal(animalId);
    }

    // Sem @Transactional de proposito, pelo mesmo motivo do instanciarPreventivo.
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

    // Preventivo ancorado em ULTIMA_REALIZACAO; pos-cirurgico fica fora (ocorrencia unica, sem recorrencia).
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
        // motivo nao e persistido — nao ha campo no schema para isso.
        plano.setStatus(StatusPlano.CANCELADO);
        plano.getItens().stream()
                .filter(e -> e.getStatus() == StatusItem.PENDENTE)
                .forEach(e -> e.setStatus(StatusItem.CANCELADO));
        planoRepository.save(plano);
    }

    // Havendo mais de um candidato, o de menor id vence — determinismo, nao criterio clinico.
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
        // Categoria copiada do protocolo — a consulta de idempotencia le este campo, nao o join.
        plano.setCategoria(CategoriaPlano.valueOf(protocolo.categoria().name()));
        plano.setStatus(StatusPlano.ATIVO);
        return plano;
    }

    // Ordenado aqui, pela data ja resolvida — offset em unidades diferentes nao e comparavel antes disso.
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

    // ULTIMA_REALIZACAO devolve nulo de proposito — regra ancorada nela nao produz item.
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
