package br.com.fiap.petbuddies.flow;

import br.com.fiap.petbuddies.client.PetNetApiClient;
import br.com.fiap.petbuddies.domain.entity.SessaoBotEntity;
import br.com.fiap.petbuddies.domain.entity.TriagemSessaoEntity;
import br.com.fiap.petbuddies.domain.enums.ClassificacaoTriagem;
import br.com.fiap.petbuddies.domain.enums.Intencao;
import br.com.fiap.petbuddies.domain.enums.TriagemStage;
import br.com.fiap.petbuddies.domain.repository.SessaoBotRepository;
import br.com.fiap.petbuddies.domain.repository.TriagemSessaoRepository;
import br.com.fiap.petbuddies.dto.bot.AtoComunicativo;
import br.com.fiap.petbuddies.dto.bot.ConversationContext;
import br.com.fiap.petbuddies.dto.client.AnimalDto;
import br.com.fiap.petbuddies.flow.dto.FlowResponse;
import br.com.fiap.petbuddies.flow.dto.TriagemScoreResultado;
import br.com.fiap.petbuddies.service.bot.RespostaParserService;
import br.com.fiap.petbuddies.service.bot.RespostaParserService.RespostaBinaria;
import br.com.fiap.petbuddies.service.bot.RespostaParserService.TempoRelatado;
import br.com.fiap.petbuddies.service.motor.TriagemScoreService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TriagemFlowService {

    private static final ZoneId ZONE_ID = ZoneId.of("America/Sao_Paulo");
    private static final String P1 = "Ele está comendo e bebendo normalmente?";
    private static final String P2 = "Você percebeu dor forte, apatia intensa ou dificuldade para respirar?";
    private static final String P3 = "Há quanto tempo isso começou?";
    private static final String P4 = "Tem sangue, vômitos repetidos, convulsão ou piora rápida?";

    private final PetNetApiClient petNetApiClient;
    private final TriagemSessaoRepository triagemSessaoRepository;
    private final SessaoBotRepository sessaoBotRepository;
    private final RespostaParserService parserService;
    private final TriagemScoreService triagemScoreService;
    private final AgendamentoFlowService agendamentoFlowService;

    public TriagemFlowService(PetNetApiClient petNetApiClient, TriagemSessaoRepository triagemSessaoRepository,
                              SessaoBotRepository sessaoBotRepository, RespostaParserService parserService,
                              TriagemScoreService triagemScoreService, AgendamentoFlowService agendamentoFlowService) {
        this.petNetApiClient = petNetApiClient;
        this.triagemSessaoRepository = triagemSessaoRepository;
        this.sessaoBotRepository = sessaoBotRepository;
        this.parserService = parserService;
        this.triagemScoreService = triagemScoreService;
        this.agendamentoFlowService = agendamentoFlowService;
    }

    @Transactional
    public FlowResponse iniciar(String telefone, String mensagem, ConversationContext ctx) {
        if (!ctx.isResponsavelIdentificado() || ctx.getResponsavelId() == null) {
            String fallback = "Para fazer a triagem, preciso identificar seu animal primeiro. Vamos fazer um cadastro rápido?";
            return FlowResponse.novoFluxoComAto(Intencao.CADASTRO,
                    AtoComunicativo.orientar(fallback, FlowSupport.dados("motivo", "tutor_nao_identificado")));
        }

        List<AnimalDto> animais = petNetApiClient.listarAnimaisDoResponsavel(ctx.getResponsavelId());
        if (animais.isEmpty()) {
            String fallback = "Para fazer a triagem, preciso cadastrar seu animal primeiro. Me diga os dados dele?";
            return FlowResponse.novoFluxoComAto(Intencao.CADASTRO,
                    AtoComunicativo.orientar(fallback, FlowSupport.dados("motivo", "animal_nao_cadastrado")));
        }

        Optional<AnimalDto> animal = FlowSupport.resolverAnimal(animais, mensagem);
        if (animal.isEmpty()) {
            atualizarSessaoTriagem(telefone, TriagemStage.TRIAGEM_IDENTIFICANDO_ANIMAL);
            String fallback = "De qual animal você está falando? " + FlowSupport.formatarAnimais(animais);
            return FlowResponse.comAto(AtoComunicativo.perguntar(fallback,
                    FlowSupport.dados("pergunta", "De qual animal você está falando?",
                            "animaisCandidatos", FlowSupport.formatarAnimais(animais))));
        }

        TriagemSessaoEntity sessao = triagemSessaoRepository
                .findFirstByTelefoneAndFinalizadaEmIsNullOrderByIniciadaEmDesc(telefone)
                .orElseGet(TriagemSessaoEntity::new);
        sessao.setTelefone(telefone);
        sessao.setPetNetApiResponsavelId(ctx.getResponsavelId());
        sessao.setPetNetApiAnimalId(animal.get().getId());
        sessao.setSintomaPrincipal(mensagem);
        sessao.setStageAtual(TriagemStage.TRIAGEM_AGUARDANDO_P1);
        sessao.setEmergenciaSuspeita(triagemScoreService.isEmergenciaSuspeita(mensagem));
        triagemSessaoRepository.save(sessao);
        atualizarSessaoTriagem(telefone, TriagemStage.TRIAGEM_AGUARDANDO_P1);

        String prefixo = Boolean.TRUE.equals(sessao.getEmergenciaSuspeita())
                ? "Pelos sinais descritos, recomendo atendimento veterinário o quanto antes. Vou fazer perguntas rápidas para confirmar a gravidade.\n"
                : "";
        String fallback = prefixo + P1;
        return FlowResponse.comAto(AtoComunicativo.perguntar(fallback,
                FlowSupport.dados("sintoma", mensagem, "pergunta", P1,
                        "orientacaoInicial", prefixo.isBlank() ? null : prefixo.trim())));
    }

    @Transactional
    public FlowResponse processar(String telefone, String mensagem) {
        TriagemSessaoEntity sessao = triagemSessaoRepository
                .findFirstByTelefoneAndFinalizadaEmIsNullOrderByIniciadaEmDesc(telefone)
                .orElse(null);
        if (sessao == null) {
            String fallback = "Não encontrei uma triagem em andamento. Me conte novamente o que está acontecendo com o animal.";
            return FlowResponse.finalizarFluxoComAto(AtoComunicativo.orientar(fallback,
                    FlowSupport.dados("motivo", "triagem_nao_encontrada")));
        }

        if (sessao.getStageAtual() == TriagemStage.TRIAGEM_OFERECEU_AGENDAMENTO) {
            return processarOfertaAgendamento(telefone, mensagem, sessao);
        }

        return switch (sessao.getStageAtual()) {
            case TRIAGEM_AGUARDANDO_P1 -> responderP1(sessao, telefone, mensagem);
            case TRIAGEM_AGUARDANDO_P2 -> responderP2(sessao, telefone, mensagem);
            case TRIAGEM_AGUARDANDO_P3 -> responderP3(sessao, telefone, mensagem);
            case TRIAGEM_AGUARDANDO_P4 -> responderP4(sessao, telefone, mensagem);
            case TRIAGEM_IDENTIFICANDO_ANIMAL -> FlowResponse.comAto(AtoComunicativo.perguntar(
                    "Preciso identificar o animal antes da triagem. Informe o nome dele.",
                    FlowSupport.dados("pergunta", "Informe o nome do animal.")));
            case TRIAGEM_FINALIZADA -> FlowResponse.finalizarFluxoComAto(AtoComunicativo.orientar(
                    "A triagem anterior já foi finalizada. Me diga como posso ajudar agora.",
                    FlowSupport.dados("motivo", "triagem_finalizada")));
            case TRIAGEM_OFERECEU_AGENDAMENTO -> processarOfertaAgendamento(telefone, mensagem, sessao);
        };
    }

    @Transactional
    public void cancelarTriagemAberta(String telefone) {
        triagemSessaoRepository.findFirstByTelefoneAndFinalizadaEmIsNullOrderByIniciadaEmDesc(telefone)
                .ifPresent(sessao -> {
                    sessao.setFinalizadaEm(LocalDateTime.now(ZONE_ID));
                    triagemSessaoRepository.save(sessao);
                });
    }

    private FlowResponse responderP1(TriagemSessaoEntity sessao, String telefone, String mensagem) {
        if (parserService.interpretarBinaria(mensagem) == RespostaBinaria.INDEFINIDO) {
            String fallback = "Para eu seguir com segurança: ele está comendo e bebendo normalmente?";
            return FlowResponse.comAto(AtoComunicativo.perguntar(fallback, FlowSupport.dados("pergunta", P1)));
        }
        sessao.setRespostaP1(mensagem);
        sessao.setStageAtual(TriagemStage.TRIAGEM_AGUARDANDO_P2);
        triagemSessaoRepository.save(sessao);
        atualizarSessaoTriagem(telefone, TriagemStage.TRIAGEM_AGUARDANDO_P2);
        return FlowResponse.comAto(AtoComunicativo.perguntar(P2, FlowSupport.dados("pergunta", P2)));
    }

    private FlowResponse responderP2(TriagemSessaoEntity sessao, String telefone, String mensagem) {
        if (parserService.interpretarBinaria(mensagem) == RespostaBinaria.INDEFINIDO) {
            return FlowResponse.comAto(AtoComunicativo.perguntar(P2, FlowSupport.dados("pergunta", P2)));
        }
        sessao.setRespostaP2(mensagem);
        sessao.setStageAtual(TriagemStage.TRIAGEM_AGUARDANDO_P3);
        triagemSessaoRepository.save(sessao);
        atualizarSessaoTriagem(telefone, TriagemStage.TRIAGEM_AGUARDANDO_P3);
        return FlowResponse.comAto(AtoComunicativo.perguntar(P3, FlowSupport.dados("pergunta", P3)));
    }

    private FlowResponse responderP3(TriagemSessaoEntity sessao, String telefone, String mensagem) {
        if (parserService.interpretarTempo(mensagem) == TempoRelatado.INDEFINIDO) {
            String fallback = "Há quanto tempo isso começou? Pode responder como 'hoje', 'desde ontem' ou 'há 2 horas'.";
            return FlowResponse.comAto(AtoComunicativo.perguntar(fallback,
                    FlowSupport.dados("pergunta", P3, "exemplos", "hoje, desde ontem ou há 2 horas")));
        }
        sessao.setRespostaP3(mensagem);
        sessao.setStageAtual(TriagemStage.TRIAGEM_AGUARDANDO_P4);
        triagemSessaoRepository.save(sessao);
        atualizarSessaoTriagem(telefone, TriagemStage.TRIAGEM_AGUARDANDO_P4);
        return FlowResponse.comAto(AtoComunicativo.perguntar(P4, FlowSupport.dados("pergunta", P4)));
    }

    private FlowResponse responderP4(TriagemSessaoEntity sessao, String telefone, String mensagem) {
        if (parserService.interpretarBinaria(mensagem) == RespostaBinaria.INDEFINIDO) {
            return FlowResponse.comAto(AtoComunicativo.perguntar(P4, FlowSupport.dados("pergunta", P4)));
        }
        sessao.setRespostaP4(mensagem);

        TriagemScoreResultado resultado = triagemScoreService.calcular(sessao);
        sessao.setScoreTriagem(resultado.getScore());
        sessao.setClassificacao(resultado.getClassificacao());
        sessao.setRecomendacao(resultado.getRecomendacao());

        if (resultado.getClassificacao() == ClassificacaoTriagem.PRIORITARIO) {
            sessao.setStageAtual(TriagemStage.TRIAGEM_OFERECEU_AGENDAMENTO);
            triagemSessaoRepository.save(sessao);
            atualizarSessaoTriagem(telefone, TriagemStage.TRIAGEM_OFERECEU_AGENDAMENTO);
            String fallback = formatarResultado(resultado)
                    + "\nQuer que eu procure horários disponíveis para consulta?";
            return FlowResponse.comAto(AtoComunicativo.informarResultado(fallback, dadosResultado(resultado,
                    "ofertaAgendamento", "Quer que eu procure horários disponíveis para consulta?")));
        }

        sessao.setStageAtual(TriagemStage.TRIAGEM_FINALIZADA);
        sessao.setFinalizadaEm(LocalDateTime.now(ZONE_ID));
        triagemSessaoRepository.save(sessao);
        String fallback = formatarResultado(resultado);
        AtoComunicativo ato = resultado.getClassificacao() == ClassificacaoTriagem.EMERGENCIA
                ? AtoComunicativo.orientarEmergencia(fallback, dadosResultado(resultado))
                : AtoComunicativo.informarResultado(fallback, dadosResultado(resultado));
        return FlowResponse.finalizarFluxoComAto(ato);
    }

    private FlowResponse processarOfertaAgendamento(String telefone, String mensagem, TriagemSessaoEntity sessao) {
        if (parserService.isConfirmacao(mensagem)) {
            sessao.setFinalizadaEm(LocalDateTime.now(ZONE_ID));
            triagemSessaoRepository.save(sessao);
            return agendamentoFlowService.iniciarAgendamento(telefone, sessao.getPetNetApiAnimalId(), null);
        }
        if (parserService.isNegacao(mensagem)) {
            sessao.setFinalizadaEm(LocalDateTime.now(ZONE_ID));
            triagemSessaoRepository.save(sessao);
            String fallback = "Tudo bem. Se os sinais piorarem, procure atendimento veterinário.";
            return FlowResponse.finalizarFluxoComAto(AtoComunicativo.orientar(fallback,
                    FlowSupport.dados("orientacao", "Se os sinais piorarem, procure atendimento veterinário.")));
        }
        String fallback = "Quer que eu procure horários disponíveis para consulta? Pode responder com sim ou não.";
        return FlowResponse.comAto(AtoComunicativo.confirmar(fallback,
                FlowSupport.dados("pergunta", "Quer que eu procure horários disponíveis para consulta?",
                        "respostasEsperadas", "sim ou não")));
    }

    private void atualizarSessaoTriagem(String telefone, TriagemStage stage) {
        SessaoBotEntity sessao = sessaoBotRepository.findById(telefone).orElseGet(SessaoBotEntity::new);
        sessao.setTelefone(telefone);
        sessao.setFluxoAtivo(Intencao.TRIAGEM);
        sessao.setStageAtual(stage.name());
        sessao.setUpdatedAt(LocalDateTime.now(ZONE_ID));
        sessaoBotRepository.save(sessao);
    }

    private static String formatarResultado(TriagemScoreResultado resultado) {
        return "Classificação: " + resultado.getClassificacao()
                + " (score " + resultado.getScore() + "). "
                + resultado.getRecomendacao();
    }

    private static Map<String, Object> dadosResultado(TriagemScoreResultado resultado, Object... extras) {
        Map<String, Object> dados = FlowSupport.dados(
                "score", resultado.getScore(),
                "classificacao", resultado.getClassificacao(),
                "recomendacao", resultado.getRecomendacao());
        for (int i = 0; i + 1 < extras.length; i += 2) {
            if (extras[i] != null && extras[i + 1] != null) {
                dados.put(String.valueOf(extras[i]), extras[i + 1]);
            }
        }
        return dados;
    }
}
