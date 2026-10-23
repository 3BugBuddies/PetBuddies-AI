package br.com.fiap.petbuddies.service.cadastro;

import br.com.fiap.petbuddies.apresentacao.Rotulos;
import br.com.fiap.petbuddies.domain.entity.AnimalEntity;
import br.com.fiap.petbuddies.domain.entity.ConsultaEntity;
import br.com.fiap.petbuddies.domain.entity.CondicaoClinicaEntity;
import br.com.fiap.petbuddies.domain.entity.ItemPlanoCuidadoEntity;
import br.com.fiap.petbuddies.domain.entity.PlanoCuidadoEntity;
import br.com.fiap.petbuddies.domain.entity.PrescricaoEntity;
import br.com.fiap.petbuddies.domain.entity.RegistroAtendimentoEntity;
import br.com.fiap.petbuddies.domain.entity.RegraPrescricaoEntity;
import br.com.fiap.petbuddies.domain.enums.atendimento.StatusConsulta;
import br.com.fiap.petbuddies.domain.enums.checkin.TipoDesfecho;
import br.com.fiap.petbuddies.domain.enums.cuidado.CategoriaPlano;
import br.com.fiap.petbuddies.domain.enums.cuidado.StatusItem;
import br.com.fiap.petbuddies.domain.enums.cuidado.StatusPlano;
import br.com.fiap.petbuddies.domain.enums.cuidado.TipoOrigemItem;
import br.com.fiap.petbuddies.domain.repository.AnimalRepository;
import br.com.fiap.petbuddies.domain.repository.CondicaoClinicaRepository;
import br.com.fiap.petbuddies.domain.repository.ConsultaRepository;
import br.com.fiap.petbuddies.domain.repository.PlanoCuidadoRepository;
import br.com.fiap.petbuddies.domain.repository.PrescricaoRepository;
import br.com.fiap.petbuddies.domain.repository.RegistroAtendimentoRepository;
import br.com.fiap.petbuddies.domain.repository.RegraPrescricaoRepository;
import br.com.fiap.petbuddies.dto.cadastro.AnimalResponse;
import br.com.fiap.petbuddies.dto.checkin.CheckinDesfechoResponse;
import br.com.fiap.petbuddies.dto.checkin.CheckinResponse;
import br.com.fiap.petbuddies.dto.checkin.CondicaoObservadaResponse;
import br.com.fiap.petbuddies.dto.paciente.FichaPacienteResponse;
import br.com.fiap.petbuddies.dto.paciente.FichaPacienteResponse.CuidadoFicha;
import br.com.fiap.petbuddies.dto.paciente.FichaPacienteResponse.EntradaLinhaTempo;
import br.com.fiap.petbuddies.dto.paciente.FichaPacienteResponse.PlanoFicha;
import br.com.fiap.petbuddies.dto.paciente.FichaPacienteResponse.TratamentoFicha;
import br.com.fiap.petbuddies.dto.paciente.FiltroPaciente;
import br.com.fiap.petbuddies.dto.paciente.PacienteResumoResponse;
import br.com.fiap.petbuddies.exception.cadastro.AnimalNaoEncontradoException;
import br.com.fiap.petbuddies.service.atendimento.ConsultaService;
import br.com.fiap.petbuddies.service.checkin.CheckinService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Leituras do painel sobre o paciente: a lista com plano e próximo cuidado, e a ficha em linha do tempo. */
@Service
public class PacientePainelService {

    private static final int RELATO_MAX = 180;
    private static final int CUIDADOS_VISIVEIS = 8;
    private static final int PLANO_FUTURO_NA_LINHA = 3;
    private static final int LINHA_MAX = 60;

    // Quem tem tratamento ou pós-cirúrgico aparece como "Em tratamento"; o preventivo vem por baixo.
    private static final List<CategoriaPlano> PRIORIDADE =
            List.of(CategoriaPlano.TRATAMENTO, CategoriaPlano.POS_CIRURGICO, CategoriaPlano.PREVENTIVO);
    private static final Set<StatusItem> ENCERRADOS =
            EnumSet.of(StatusItem.REALIZADO, StatusItem.SUSPENSO, StatusItem.ENCAMINHADO);

    private final AnimalRepository animalRepository;
    private final PlanoCuidadoRepository planoRepository;
    private final ConsultaRepository consultaRepository;
    private final RegistroAtendimentoRepository registroRepository;
    private final PrescricaoRepository prescricaoRepository;
    private final RegraPrescricaoRepository regraRepository;
    private final CondicaoClinicaRepository condicaoRepository;
    private final CheckinService checkinService;
    private final Rotulos rotulos;

    public PacientePainelService(
            AnimalRepository animalRepository,
            PlanoCuidadoRepository planoRepository,
            ConsultaRepository consultaRepository,
            RegistroAtendimentoRepository registroRepository,
            PrescricaoRepository prescricaoRepository,
            RegraPrescricaoRepository regraRepository,
            CondicaoClinicaRepository condicaoRepository,
            CheckinService checkinService,
            Rotulos rotulos) {
        this.animalRepository = animalRepository;
        this.planoRepository = planoRepository;
        this.consultaRepository = consultaRepository;
        this.registroRepository = registroRepository;
        this.prescricaoRepository = prescricaoRepository;
        this.regraRepository = regraRepository;
        this.condicaoRepository = condicaoRepository;
        this.checkinService = checkinService;
        this.rotulos = rotulos;
    }

    @Transactional(readOnly = true)
    public List<PacienteResumoResponse> listar(String busca, FiltroPaciente filtro) {
        List<AnimalEntity> animais = busca == null || busca.isBlank()
                ? animalRepository.findAllComResponsavel()
                : animalRepository.pesquisarComResponsavel("%" + busca.trim().toLowerCase() + "%");
        if (animais.isEmpty()) {
            return List.of();
        }
        Map<Long, List<PlanoCuidadoEntity>> planos = planoRepository
                .findPorAnimaisEStatus(animais.stream().map(AnimalEntity::getId).toList(), StatusPlano.ATIVO).stream()
                .collect(Collectors.groupingBy(PlanoCuidadoEntity::getAnimalId));
        LocalDate hoje = LocalDate.now();

        return animais.stream()
                .sorted(Comparator.comparing(AnimalEntity::getNome, String.CASE_INSENSITIVE_ORDER))
                .filter(a -> combina(planos.getOrDefault(a.getId(), List.of()), filtro))
                .map(a -> resumir(a, planos.getOrDefault(a.getId(), List.of()), hoje))
                .toList();
    }

    @Transactional(readOnly = true)
    public FichaPacienteResponse ficha(Long animalId) {
        AnimalEntity animal = animalRepository.findComResponsavelById(animalId)
                .orElseThrow(() -> new AnimalNaoEncontradoException(animalId));
        LocalDate hoje = LocalDate.now();

        List<PlanoCuidadoEntity> planosAtivos = planoRepository
                .findPorAnimaisEStatus(List.of(animalId), StatusPlano.ATIVO).stream()
                .sorted(Comparator.comparingInt(p -> PRIORIDADE.indexOf(p.getCategoria())))
                .toList();
        List<PlanoFicha> planos = planosAtivos.stream().map(p -> montarPlano(p, hoje)).toList();

        List<ConsultaEntity> consultas = consultaRepository.findByAnimalIdOrderByDataHoraDesc(animalId);
        List<EntradaLinhaTempo> linha = new ArrayList<>();
        linha.addAll(entradasDeCheckin(animalId));
        linha.addAll(entradasDeConsulta(animalId, consultas));
        linha.addAll(entradasDePlano(planosAtivos, hoje));
        List<EntradaLinhaTempo> ordenada = linha.stream()
                .sorted(Comparator.comparing(EntradaLinhaTempo::quando).reversed())
                .limit(LINHA_MAX)
                .toList();

        return new FichaPacienteResponse(
                AnimalResponse.from(animal), idade(animal.getDataNascimento(), hoje),
                consultaAberta(consultas, hoje), planos, ordenada);
    }

    private boolean combina(List<PlanoCuidadoEntity> planos, FiltroPaciente filtro) {
        boolean emTratamento = planos.stream().anyMatch(p -> p.getCategoria() != CategoriaPlano.PREVENTIVO);
        return switch (filtro) {
            case TODOS -> true;
            case TRATAMENTO -> emTratamento;
            case PREVENTIVO -> !planos.isEmpty() && !emTratamento;
            case SEM_PLANO -> planos.isEmpty();
        };
    }

    private PacienteResumoResponse resumir(AnimalEntity animal, List<PlanoCuidadoEntity> planos, LocalDate hoje) {
        PlanoCuidadoEntity principal = planos.stream()
                .min(Comparator.comparingInt(p -> PRIORIDADE.indexOf(p.getCategoria())))
                .orElse(null);
        LocalDate planoAte = principal == null || principal.getCategoria() == CategoriaPlano.PREVENTIVO ? null
                : principal.getItens().stream()
                        .filter(i -> i.getStatus() != StatusItem.CANCELADO)
                        .map(ItemPlanoCuidadoEntity::getDataAlvo)
                        .max(Comparator.naturalOrder()).orElse(null);
        ItemPlanoCuidadoEntity proximo = planos.stream()
                .flatMap(p -> p.getItens().stream())
                .filter(i -> i.getStatus() == StatusItem.PENDENTE || i.getStatus() == StatusItem.ATRASADO)
                .min(Comparator.comparing(ItemPlanoCuidadoEntity::getDataAlvo))
                .orElse(null);
        return new PacienteResumoResponse(
                animal.getId(), animal.getNome(), animal.getEspecie(), animal.getRaca(), animal.getSexo(),
                animal.getResponsavel().getNome(),
                animal.getResponsavel().getContato() == null ? null : animal.getResponsavel().getContato().getTelefone(),
                principal == null ? null : principal.getCategoria(), planoAte,
                proximo == null ? null : proximo.getNome(),
                proximo == null ? null : proximo.getDataAlvo(),
                proximo != null && proximo.getDataAlvo().isBefore(hoje));
    }

    private PlanoFicha montarPlano(PlanoCuidadoEntity plano, LocalDate hoje) {
        List<ItemPlanoCuidadoEntity> itens = plano.getItens().stream()
                .filter(i -> i.getStatus() != StatusItem.CANCELADO)
                .sorted(Comparator.comparing(ItemPlanoCuidadoEntity::getDataAlvo))
                .toList();
        List<TratamentoFicha> tratamentos = tratamentos(itens);
        List<ItemPlanoCuidadoEntity> molde = itens.stream().filter(i -> i.getOrigem() != TipoOrigemItem.PRESCRICAO).toList();
        List<ItemPlanoCuidadoEntity> abertos = molde.stream().filter(i -> !ENCERRADOS.contains(i.getStatus())).toList();
        List<CuidadoFicha> cuidados = abertos.stream()
                .limit(CUIDADOS_VISIVEIS)
                .map(i -> new CuidadoFicha(i.getNome(), i.getTipo(), i.getDataAlvo(), i.getStatus(),
                        i.getStatus() == StatusItem.ATRASADO || i.getDataAlvo().isBefore(hoje)))
                .toList();
        return new PlanoFicha(plano.getCategoria(), tratamentos, cuidados,
                Math.max(0, abertos.size() - CUIDADOS_VISIVEIS), molde.size() - abertos.size());
    }

    private List<TratamentoFicha> tratamentos(List<ItemPlanoCuidadoEntity> itens) {
        Map<Long, List<ItemPlanoCuidadoEntity>> porPrescricao = itens.stream()
                .filter(i -> i.getPrescricaoId() != null)
                .collect(Collectors.groupingBy(ItemPlanoCuidadoEntity::getPrescricaoId));
        if (porPrescricao.isEmpty()) {
            return List.of();
        }
        return prescricaoRepository.findAllById(porPrescricao.keySet()).stream()
                .sorted(Comparator.comparing(PrescricaoEntity::getDataInicio))
                .map(p -> tratamento(p, porPrescricao.get(p.getId())))
                .toList();
    }

    private TratamentoFicha tratamento(PrescricaoEntity prescricao, List<ItemPlanoCuidadoEntity> itens) {
        BigDecimal min = prescricao.getFaixaDose().getDoseMin();
        BigDecimal max = prescricao.getFaixaDose().getDoseMax();
        String dose = (min.compareTo(max) == 0 ? rotulos.numero(min) : rotulos.numero(min) + "–" + rotulos.numero(max))
                + " " + prescricao.getFaixaDose().getUnidade();
        String frequencia = prescricao.getFrequenciaDia() + "×/dia · " + prescricao.getDuracaoDias() + " dias";
        List<String> regras = regraRepository.findByPrescricaoIdOrderByOrdemAsc(prescricao.getId()).stream()
                .map(r -> rotulos.regra(r.getCondicaoCongelada().getRotulo(), r.getOperador(), r.getLimite(),
                        r.getCondicaoClinica().getUnidade(), r.getAcaoDose()))
                .toList();
        int total = itens.size();
        int registrados = (int) itens.stream().filter(i -> ENCERRADOS.contains(i.getStatus())).count();
        return new TratamentoFicha(prescricao.getMedicamento(), dose, frequencia, prescricao.getDataInicio(),
                prescricao.getDataInicio().plusDays(Math.max(0, prescricao.getDuracaoDias() - 1L)), regras,
                registrados, total, total == 0 ? 0 : registrados * 100 / total);
    }

    private List<EntradaLinhaTempo> entradasDeCheckin(Long animalId) {
        List<CheckinResponse> checkins = checkinService.listar(animalId);
        if (checkins.isEmpty()) {
            return List.of();
        }
        Set<Long> condicaoIds = checkins.stream()
                .flatMap(c -> c.getCondicoesObservadas().stream())
                .map(CondicaoObservadaResponse::getCondicaoClinicaId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, CondicaoClinicaEntity> condicoes = condicaoRepository.findAllById(condicaoIds).stream()
                .collect(Collectors.toMap(CondicaoClinicaEntity::getId, Function.identity()));
        Set<Long> regraIds = checkins.stream()
                .flatMap(c -> c.getDesfechos().stream())
                .map(CheckinDesfechoResponse::getRegraAplicadaId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, RegraPrescricaoEntity> regras = regraRepository.findAllById(regraIds).stream()
                .collect(Collectors.toMap(RegraPrescricaoEntity::getId, Function.identity()));

        return checkins.stream().map(c -> entradaDeCheckin(c, condicoes, regras)).toList();
    }

    private EntradaLinhaTempo entradaDeCheckin(
            CheckinResponse checkin, Map<Long, CondicaoClinicaEntity> condicoes, Map<Long, RegraPrescricaoEntity> regras) {
        boolean acionou = checkin.getDesfechos().stream().anyMatch(d -> d.getDesfecho() == TipoDesfecho.ACIONAR_CLINICA);
        boolean alarme = checkin.isEscalado() || acionou;

        List<String> efeitos = new ArrayList<>();
        String sinais = checkin.getCondicoesObservadas().stream()
                .filter(o -> o.getCondicaoClinicaId() != null && condicoes.containsKey(o.getCondicaoClinicaId()))
                .filter(o -> condicoes.get(o.getCondicaoClinicaId()).isCritica())
                .filter(o -> Boolean.TRUE.equals(o.getValorBooleano()) || o.getValorNumerico() != null)
                .map(o -> condicoes.get(o.getCondicaoClinicaId()).getRotulo().toLowerCase())
                .distinct().collect(Collectors.joining(", "));
        if (!sinais.isEmpty()) {
            efeitos.add("sinal de alarme (" + sinais + ") → acionar clínica");
        }
        for (CheckinDesfechoResponse d : checkin.getDesfechos()) {
            efeitos.add(efeitoDaDose(d, regras.get(d.getRegraAplicadaId())));
        }
        if (efeitos.isEmpty()) {
            efeitos.add("nenhuma regra se aplicou");
        }

        boolean semDose = checkin.getDesfechos().stream().anyMatch(d -> d.getDesfecho() == TipoDesfecho.SEM_DOSE);
        boolean dose = checkin.getDesfechos().stream().anyMatch(d -> d.getDesfecho() == TipoDesfecho.DOSE_CALCULADA);
        String selo;
        String estilo;
        if (alarme) {
            selo = "alarme";
            estilo = "alerta";
        } else if (dose) {
            selo = "dose";
            estilo = "ok";
        } else if (semDose) {
            selo = "sem dose";
            estilo = "espera";
        } else {
            selo = "sem regra";
            estilo = "neutro";
        }
        return new EntradaLinhaTempo("CHECKIN", checkin.getRegistradoEm(), true, selo, estilo, "Check-in",
                "“" + resumo(checkin.getNarrativa()) + "” → " + String.join("; ", efeitos));
    }

    private String efeitoDaDose(CheckinDesfechoResponse d, RegraPrescricaoEntity regra) {
        String remedio = d.getMedicamento() == null ? "" : d.getMedicamento() + ": ";
        if (d.getDesfecho() == null) {
            return remedio + "sem desfecho";
        }
        return switch (d.getDesfecho()) {
            case ACIONAR_CLINICA -> remedio + "acionar clínica";
            case SEM_DOSE -> remedio + "sem dose";
            case DOSE_CALCULADA -> remedio + (regra == null ? "" : rotulos.de(regra.getAcaoDose()) + " · ")
                    + rotulos.numero(d.getDoseAplicada()) + " " + (d.getUnidade() == null ? "" : d.getUnidade());
        };
    }

    private List<EntradaLinhaTempo> entradasDeConsulta(Long animalId, List<ConsultaEntity> consultas) {
        Map<Long, RegistroAtendimentoEntity> registroPorConsulta = registroRepository
                .findByAnimalIdOrderByDataAtendimentoDesc(animalId).stream()
                .filter(r -> r.getConsulta() != null)
                .collect(Collectors.toMap(r -> r.getConsulta().getId(), Function.identity(), (a, b) -> a));
        Map<Long, List<PrescricaoEntity>> receitaPorRegistro = prescricaoRepository
                .findByAnimalIdOrderByDataInicioDesc(animalId).stream()
                .collect(Collectors.groupingBy(p -> p.getRegistroAtendimento().getId()));

        LocalDateTime agora = LocalDateTime.now();
        ConsultaEntity proxima = consultas.stream()
                .filter(c -> ConsultaService.STATUS_FECHAVEL.contains(c.getStatus()) && !c.getDataHora().isBefore(agora))
                .min(Comparator.comparing(ConsultaEntity::getDataHora)).orElse(null);

        List<EntradaLinhaTempo> entradas = new ArrayList<>();
        for (ConsultaEntity c : consultas) {
            String tipo = rotulos.de(c.getTipo());
            String vet = c.getVeterinario().getNome();
            String selo = rotulos.de(c.getStatus());
            String estilo = c.getStatus() == StatusConsulta.REALIZADA ? "neutro" : "espera";
            String detalhe = "Com " + vet;
            if (c == proxima) {
                selo = "próxima";
                estilo = "info";
            }
            RegistroAtendimentoEntity registro = registroPorConsulta.get(c.getId());
            List<PrescricaoEntity> receitas = registro == null ? List.of()
                    : receitaPorRegistro.getOrDefault(registro.getId(), List.of());
            if (!receitas.isEmpty()) {
                detalhe = "Receita de " + receitas.stream().map(PrescricaoEntity::getMedicamento).collect(Collectors.joining(", "))
                        + " assinada por " + receitas.get(0).getVeterinario().getNome();
            } else if (registro != null && registro.getDiagnostico() != null && !registro.getDiagnostico().isBlank()) {
                detalhe = "Diagnóstico: " + resumo(registro.getDiagnostico()) + " · com " + vet;
            }
            entradas.add(new EntradaLinhaTempo("CONSULTA", c.getDataHora(), true, selo, estilo,
                    tipo + " · " + rotulos.de(c.getStatus()).toLowerCase(), detalhe));
        }
        return entradas;
    }

    private List<EntradaLinhaTempo> entradasDePlano(List<PlanoCuidadoEntity> planos, LocalDate hoje) {
        List<ItemPlanoCuidadoEntity> molde = planos.stream()
                .flatMap(p -> p.getItens().stream())
                .filter(i -> i.getOrigem() != TipoOrigemItem.PRESCRICAO && i.getStatus() != StatusItem.CANCELADO)
                .toList();
        List<EntradaLinhaTempo> entradas = new ArrayList<>();
        for (ItemPlanoCuidadoEntity i : molde) {
            if (ENCERRADOS.contains(i.getStatus())) {
                LocalDateTime quando = i.getExecutadoEm() != null ? i.getExecutadoEm() : i.getDataAlvo().atStartOfDay();
                entradas.add(new EntradaLinhaTempo("PLANO", quando, i.getExecutadoEm() != null, "feito", "ok",
                        i.getNome(), rotulos.statusItem(i.getStatus(), false) + " no plano preventivo"));
            } else if (i.getDataAlvo().isBefore(hoje) || i.getStatus() == StatusItem.ATRASADO) {
                entradas.add(new EntradaLinhaTempo("PLANO", i.getDataAlvo().atStartOfDay(), false, "vencido", "espera",
                        i.getNome(), "Previsto no plano para esta data e ainda sem registro"));
            }
        }
        molde.stream()
                .filter(i -> !ENCERRADOS.contains(i.getStatus()) && !i.getDataAlvo().isBefore(hoje))
                .sorted(Comparator.comparing(ItemPlanoCuidadoEntity::getDataAlvo))
                .limit(PLANO_FUTURO_NA_LINHA)
                .forEach(i -> entradas.add(new EntradaLinhaTempo("PLANO", i.getDataAlvo().atStartOfDay(), false,
                        "previsto", "info", i.getNome(), "Previsto no plano")));
        return entradas;
    }

    // A consulta de hoje, se houver; senão a mais próxima ainda por fechar.
    private Long consultaAberta(Collection<ConsultaEntity> consultas, LocalDate hoje) {
        List<ConsultaEntity> abertas = consultas.stream()
                .filter(c -> ConsultaService.STATUS_FECHAVEL.contains(c.getStatus()))
                .sorted(Comparator.comparing(ConsultaEntity::getDataHora))
                .toList();
        return abertas.stream().filter(c -> c.getDataHora().toLocalDate().equals(hoje)).findFirst()
                .or(() -> abertas.stream().findFirst())
                .map(ConsultaEntity::getId).orElse(null);
    }

    private static String idade(LocalDate nascimento, LocalDate hoje) {
        Period p = Period.between(nascimento, hoje);
        if (p.getYears() >= 1) {
            return p.getYears() + (p.getYears() == 1 ? " ano" : " anos");
        }
        if (p.getMonths() >= 1) {
            return p.getMonths() + (p.getMonths() == 1 ? " mês" : " meses");
        }
        return p.getDays() + (p.getDays() == 1 ? " dia" : " dias");
    }

    private static String resumo(String texto) {
        if (texto == null) {
            return "";
        }
        String limpo = texto.strip().replaceAll("\\s+", " ");
        return limpo.length() <= RELATO_MAX ? limpo : limpo.substring(0, RELATO_MAX - 1) + "…";
    }
}
