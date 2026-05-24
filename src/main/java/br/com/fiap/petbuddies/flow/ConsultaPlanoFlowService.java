package br.com.fiap.petbuddies.flow;

import br.com.fiap.petbuddies.client.PetNetApiClient;
import br.com.fiap.petbuddies.domain.entity.SessaoBotEntity;
import br.com.fiap.petbuddies.domain.enums.Intencao;
import br.com.fiap.petbuddies.domain.repository.SessaoBotRepository;
import br.com.fiap.petbuddies.dto.bot.AtoComunicativo;
import br.com.fiap.petbuddies.dto.bot.ConversationContext;
import br.com.fiap.petbuddies.dto.client.AnimalDto;
import br.com.fiap.petbuddies.dto.motor.EventoPlanoDto;
import br.com.fiap.petbuddies.dto.motor.FatorRiscoDto;
import br.com.fiap.petbuddies.dto.motor.PlanoResponse;
import br.com.fiap.petbuddies.dto.motor.ScoreResponse;
import br.com.fiap.petbuddies.flow.dto.AnimalResumo;
import br.com.fiap.petbuddies.flow.dto.DadosConsultaPlanoPendente;
import br.com.fiap.petbuddies.flow.dto.FlowResponse;
import br.com.fiap.petbuddies.service.motor.MotorPlanoService;
import br.com.fiap.petbuddies.service.motor.MotorScoreService;
import br.com.fiap.petbuddies.service.bot.RespostaParserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ConsultaPlanoFlowService {

    public static final String STAGE_AGUARDANDO_ESCOLHA_ANIMAL = "CONSULTA_PLANO_AGUARDANDO_ESCOLHA_ANIMAL";

    private static final Logger log = LoggerFactory.getLogger(ConsultaPlanoFlowService.class);
    private static final ZoneId ZONE_ID = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PetNetApiClient petNetApiClient;
    private final MotorPlanoService motorPlanoService;
    private final MotorScoreService motorScoreService;
    private final SessaoBotRepository sessaoBotRepository;
    private final FlowSessaoHelper sessaoHelper;
    private final RespostaParserService parserService;

    public ConsultaPlanoFlowService(PetNetApiClient petNetApiClient,
                                    MotorPlanoService motorPlanoService,
                                    MotorScoreService motorScoreService,
                                    SessaoBotRepository sessaoBotRepository,
                                    FlowSessaoHelper sessaoHelper,
                                    RespostaParserService parserService) {
        this.petNetApiClient = petNetApiClient;
        this.motorPlanoService = motorPlanoService;
        this.motorScoreService = motorScoreService;
        this.sessaoBotRepository = sessaoBotRepository;
        this.sessaoHelper = sessaoHelper;
        this.parserService = parserService;
    }

    @Transactional
    public FlowResponse iniciar(String telefone, String mensagem, ConversationContext ctx) {
        if (!ctx.isResponsavelIdentificado() || ctx.getResponsavelId() == null) {
            String fallback = "Para consultar o plano, preciso cadastrar o tutor e o animal primeiro. Vamos fazer um cadastro rápido?";
            return FlowResponse.novoFluxoComAto(Intencao.CADASTRO,
                    AtoComunicativo.orientar(fallback, FlowSupport.dados("motivo", "tutor_nao_identificado")));
        }

        List<AnimalDto> animais = petNetApiClient.listarAnimaisDoResponsavel(ctx.getResponsavelId());
        if (animais.isEmpty()) {
            String fallback = "Para consultar o plano, preciso cadastrar seu animal primeiro. Me diga os dados dele?";
            return FlowResponse.novoFluxoComAto(Intencao.CADASTRO,
                    AtoComunicativo.orientar(fallback, FlowSupport.dados("motivo", "animal_nao_cadastrado")));
        }

        Optional<AnimalDto> animal = FlowSupport.resolverAnimal(animais, mensagem);
        if (animal.isEmpty()) {
            salvarEscolhaAnimal(telefone, ctx.getResponsavelId(), animais);
            String fallback = "De qual animal você quer consultar o plano? " + FlowSupport.formatarAnimais(animais);
            return FlowResponse.comAto(AtoComunicativo.perguntar(fallback,
                    FlowSupport.dados("animaisCandidatos", nomesAnimais(animais))));
        }

        return consultarPlano(telefone, animal.get().getId(), animal.get().getNome());
    }

    @Transactional
    public FlowResponse processar(String telefone, String mensagem) {
        SessaoBotEntity sessao = sessaoBotRepository.findById(telefone).orElse(null);
        if (sessao == null || !STAGE_AGUARDANDO_ESCOLHA_ANIMAL.equals(sessao.getStageAtual())) {
            String fallback = "Não encontrei uma consulta de plano em andamento. Me diga novamente qual plano você quer consultar.";
            return FlowResponse.finalizarFluxoComAto(AtoComunicativo.orientar(fallback,
                    FlowSupport.dados("motivo", "sessao_inexistente")));
        }

        DadosConsultaPlanoPendente dadosPendentes = lerDados(sessao);
        Optional<AnimalResumo> animal = FlowSupport.resolverAnimalResumo(dadosPendentes.getAnimaisCandidatos(), mensagem, parserService);
        if (animal.isEmpty()) {
            String fallback = "Não consegui identificar qual animal. Escolha pelo nome ou número: "
                    + FlowSupport.formatarAnimaisResumo(dadosPendentes.getAnimaisCandidatos());
            return FlowResponse.comAto(AtoComunicativo.perguntar(fallback,
                    FlowSupport.dados("animaisCandidatos", nomesAnimaisResumo(dadosPendentes.getAnimaisCandidatos()))));
        }

        return consultarPlano(telefone, animal.get().getAnimalId(), animal.get().getNome());
    }

    private FlowResponse consultarPlano(String telefone, Long animalId, String animalNome) {
        try {
            Optional<PlanoResponse> planoOpt = motorPlanoService.buscarPlanoAtivo(animalId);
            Optional<ScoreResponse> scoreOpt = consultarScore(animalId);

            Map<String, Object> dados = new LinkedHashMap<>();
            dados.put("animalNome", FlowSupport.nomeOuAnimal(animalNome));
            preencherDadosPlano(dados, planoOpt.orElse(null));
            preencherDadosScore(dados, scoreOpt.orElse(null));

            String fallback = formatarFallback(animalNome, planoOpt.orElse(null), scoreOpt.orElse(null));
            return FlowResponse.finalizarFluxoComAto(AtoComunicativo.informarResultado(
                    fallback, dados, AtoComunicativo.Liberdade.AMPLA));
        } catch (Exception e) {
            log.warn("[CONSULTA_PLANO] falha tel={} animalId={}: {}", telefone, animalId, e.getMessage());
            String fallback = "Não consegui consultar o plano do animal agora. Pode tentar novamente em instantes?";
            return FlowResponse.finalizarFluxoComAto(AtoComunicativo.orientar(fallback,
                    FlowSupport.dados("motivo", "servico_indisponivel")));
        }
    }

    private Optional<ScoreResponse> consultarScore(Long animalId) {
        try {
            return Optional.of(motorScoreService.buscarMaisRecente(animalId)
                    .orElseGet(() -> motorScoreService.recalcular(animalId, "CONSULTA_PLANO")));
        } catch (Exception e) {
            log.warn("[CONSULTA_PLANO] falha ao consultar score animalId={}: {}", animalId, e.getMessage());
            return Optional.empty();
        }
    }

    private void preencherDadosPlano(Map<String, Object> dados, PlanoResponse plano) {
        if (plano == null) {
            dados.put("planoEncontrado", false);
            return;
        }
        dados.put("planoEncontrado", true);
        dados.put("plano", plano.getProtocoloNome());
        dados.put("status", plano.getStatus());

        List<EventoPlanoDto> proximos = proximosEventos(plano);
        if (!proximos.isEmpty()) {
            List<Map<String, Object>> eventosLista = new ArrayList<>();
            for (EventoPlanoDto e : proximos) {
                Map<String, Object> evento = new LinkedHashMap<>();
                evento.put("nome", e.getNome() == null ? "" : e.getNome());
                evento.put("dataAlvo", e.getDataAlvo() == null ? "sem data" : DATA.format(e.getDataAlvo()));
                eventosLista.add(evento);
            }
            dados.put("proximosEventos", eventosLista);
        }
    }

    private void preencherDadosScore(Map<String, Object> dados, ScoreResponse score) {
        if (score == null) {
            dados.put("scoreEncontrado", false);
            return;
        }
        dados.put("scoreEncontrado", true);
        dados.put("score", score.getScore());
        dados.put("classificacao", score.getClassificacao());
        List<String> fatores = fatoresPrincipais(score);
        if (!fatores.isEmpty()) {
            dados.put("fatores", fatores);
        }
    }

    private String formatarFallback(String animalNome, PlanoResponse plano, ScoreResponse score) {
        String nome = FlowSupport.nomeOuAnimal(animalNome);
        List<String> partes = new ArrayList<>();

        if (plano == null) {
            partes.add("Não encontrei plano ativo para " + nome + ".");
        } else {
            StringBuilder sb = new StringBuilder("Plano de ").append(nome)
                    .append(": ").append(plano.getProtocoloNome())
                    .append(" (").append(plano.getStatus()).append(").");
            List<EventoPlanoDto> proximos = proximosEventos(plano);
            if (!proximos.isEmpty()) {
                sb.append(" Próximos cuidados:");
                for (EventoPlanoDto e : proximos) {
                    sb.append(" ").append(e.getNome())
                            .append(" em ").append(e.getDataAlvo() == null ? "sem data" : DATA.format(e.getDataAlvo()))
                            .append(";");
                }
            }
            partes.add(sb.toString());
        }

        if (score != null) {
            StringBuilder sb = new StringBuilder("Score atual: ")
                    .append(score.getScore())
                    .append(" (").append(score.getClassificacao()).append(").");
            List<String> fatores = fatoresPrincipais(score);
            if (!fatores.isEmpty()) {
                sb.append(" Fatores: ").append(String.join("; ", fatores)).append(".");
            }
            partes.add(sb.toString());
        }

        return String.join(" ", partes);
    }

    private List<EventoPlanoDto> proximosEventos(PlanoResponse plano) {
        if (plano.getEventos() == null) return List.of();
        LocalDate hoje = LocalDate.now(ZONE_ID);
        return plano.getEventos().stream()
                .filter(e -> "PENDENTE".equals(e.getStatus()))
                .filter(e -> e.getDataAlvo() == null || !e.getDataAlvo().isBefore(hoje))
                .sorted(Comparator.comparing(EventoPlanoDto::getDataAlvo, Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(3)
                .toList();
    }

    private List<String> fatoresPrincipais(ScoreResponse score) {
        if (score.getFatores() == null) return List.of();
        return score.getFatores().stream()
                .filter(f -> f.getValor() != null && f.getValor() > 0)
                .limit(3)
                .map(FatorRiscoDto::getDescricao)
                .collect(Collectors.toList());
    }

    private void salvarEscolhaAnimal(String telefone, Long responsavelId, List<AnimalDto> animais) {
        DadosConsultaPlanoPendente dados = new DadosConsultaPlanoPendente();
        dados.setResponsavelId(responsavelId);
        dados.setAnimaisCandidatos(FlowSupport.toAnimaisResumo(animais));

        SessaoBotEntity sessao = sessaoBotRepository.findById(telefone).orElseGet(SessaoBotEntity::new);
        sessao.setTelefone(telefone);
        sessao.setFluxoAtivo(Intencao.CONSULTA_PLANO);
        sessao.setStageAtual(STAGE_AGUARDANDO_ESCOLHA_ANIMAL);
        sessao.setAcaoPendente(null);
        sessao.setDadosPendentesJson(sessaoHelper.toJson(dados));
        sessaoBotRepository.save(sessao);
    }

    private DadosConsultaPlanoPendente lerDados(SessaoBotEntity sessao) {
        DadosConsultaPlanoPendente resultado = sessaoHelper.lerDados(sessao.getDadosPendentesJson(), DadosConsultaPlanoPendente.class);
        if (resultado == null) {
            log.warn("[CONSULTA_PLANO] dadosPendentesJson inválido tel={}", sessao.getTelefone());
            return new DadosConsultaPlanoPendente();
        }
        return resultado;
    }

    private static List<String> nomesAnimais(List<AnimalDto> animais) {
        return animais.stream()
                .map(a -> a.getNome() + " (" + a.getEspecie() + ")")
                .collect(Collectors.toList());
    }

    private static List<String> nomesAnimaisResumo(List<AnimalResumo> animais) {
        return animais.stream()
                .map(a -> a.getNome() + " (" + a.getEspecie() + ")")
                .collect(Collectors.toList());
    }
}
