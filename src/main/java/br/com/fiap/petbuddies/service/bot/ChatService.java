package br.com.fiap.petbuddies.service.bot;

import br.com.fiap.petbuddies.client.PetNetApiClient;
import br.com.fiap.petbuddies.domain.entity.SessaoBotEntity;
import br.com.fiap.petbuddies.domain.enums.CadastroStage;
import br.com.fiap.petbuddies.domain.enums.Intencao;
import br.com.fiap.petbuddies.domain.repository.SessaoBotRepository;
import br.com.fiap.petbuddies.dto.bot.ConversationContext;
import br.com.fiap.petbuddies.dto.bot.IntentResult;
import br.com.fiap.petbuddies.flow.AgendamentoFlowService;
import br.com.fiap.petbuddies.flow.CadastroFlowService;
import br.com.fiap.petbuddies.flow.ConsultaPlanoFlowService;
import br.com.fiap.petbuddies.flow.TriagemFlowService;
import br.com.fiap.petbuddies.flow.dto.FlowResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    private static final ZoneId ZONE_ID = ZoneId.of("America/Sao_Paulo");
    private static final String GERAL_SYSTEM = """
          Você é o Bug 🐞, assistente oficial de WhatsApp da PetBuddies  🐾 para uma clínica veterinária.
          Responda sempre em português brasileiro, com tom acolhedor, claro e conciso.
          Máximo de 3 linhas curtas por resposta.

          [CONTEXTO]
          O CONTEXTO indicará se o tutor já está cadastrado e seu nome.
          Se identificado, use o nome dele naturalmente. Se não, não peça dados.

          [CAPACIDADES DO BOT]
          Quando o tutor perguntar como fazer algo, oriente-o com a frase exata:
          - Cadastro: diga "quero me cadastrar" ou "quero cadastrar meu animal"
          - Agendar consulta: diga "quero agendar uma consulta"
          - Ver sintomas / urgência: descreva o que está sentindo
          - Ver plano de cuidados: diga "quero ver o plano do meu pet"

          [REGRAS]
          - Saudações: apresente-se como Bug da PetBuddies e pergunte como pode ajudar.
          - Dúvidas gerais de cuidado: responda de forma educativa e genérica.
          - Nunca diagnostique doenças nem sugira medicação.
          - Nunca colete dados do tutor — oriente-o a usar o fluxo adequado.
          - Nunca prometa ações que ainda não aconteceram.
          - Se não souber responder, oriente o tutor a contatar a clínica.
            """;

    private final ChatClient chatClient;
    private final ClassificadorService classificador;
    private final PetNetApiClient petNetApiClient;
    private final SessaoBotRepository sessaoBotRepository;
    private final ChatMemory chatMemory;
    private final TriagemFlowService triagemFlowService;
    private final AgendamentoFlowService agendamentoFlowService;
    private final ConsultaPlanoFlowService consultaPlanoFlowService;
    private final CadastroFlowService cadastroFlowService;
    private final RedatorService redatorService;

    public ChatService(ChatClient chatClient, ClassificadorService classificador, PetNetApiClient petNetApiClient, SessaoBotRepository sessaoBotRepository, ChatMemory chatMemory,TriagemFlowService triagemFlowService, AgendamentoFlowService agendamentoFlowService, ConsultaPlanoFlowService consultaPlanoFlowService, CadastroFlowService cadastroFlowService, RedatorService redatorService) {
        this.chatClient = chatClient;
        this.classificador = classificador;
        this.petNetApiClient = petNetApiClient;
        this.sessaoBotRepository = sessaoBotRepository;
        this.chatMemory = chatMemory;
        this.triagemFlowService = triagemFlowService;
        this.agendamentoFlowService = agendamentoFlowService;
        this.consultaPlanoFlowService = consultaPlanoFlowService;
        this.cadastroFlowService = cadastroFlowService;
        this.redatorService = redatorService;
    }

    public String responder(String telefone, String mensagem) {
        try {
            Optional<FlowResponse> comandoGlobal = processarComandoGlobal(telefone, mensagem);
            if (comandoGlobal.isPresent()) {
                return aplicar(comandoGlobal.get(), telefone);
            }

            Optional<SessaoBotEntity> sessaoAtiva = buscarSessaoValida(telefone);
            if (sessaoAtiva.map(SessaoBotEntity::getFluxoAtivo).filter(Intencao.CADASTRO::equals).isPresent()
                    && sessaoAtiva.get().getStageAtual() != null) {
                return aplicar(cadastroFlowService.processar(telefone, mensagem), telefone);
            }
            if (sessaoAtiva.map(SessaoBotEntity::getFluxoAtivo).filter(Intencao.TRIAGEM::equals).isPresent()) {
                return aplicar(triagemFlowService.processar(telefone, mensagem), telefone);
            }
            if (sessaoAtiva.map(SessaoBotEntity::getFluxoAtivo).filter(Intencao.AGENDAMENTO::equals).isPresent()
                    && sessaoAtiva.get().getAcaoPendente() != null) {
                return aplicar(agendamentoFlowService.processar(telefone, mensagem), telefone);
            }
            if (sessaoAtiva.map(SessaoBotEntity::getFluxoAtivo).filter(Intencao.CONSULTA_PLANO::equals).isPresent()
                    && ConsultaPlanoFlowService.STAGE_AGUARDANDO_ESCOLHA_ANIMAL.equals(sessaoAtiva.get().getStageAtual())) {
                return aplicar(consultaPlanoFlowService.processar(telefone, mensagem), telefone);
            }

            Intencao intencao = resolverIntencao(telefone, mensagem);
            ConversationContext ctx = montarContexto(telefone, intencao);

            if (intencao == Intencao.CADASTRO) {
                return aplicar(cadastroFlowService.iniciar(telefone, mensagem, ctx), telefone);
            }
            if (intencao == Intencao.TRIAGEM) {
                return aplicar(triagemFlowService.iniciar(telefone, mensagem, ctx), telefone);
            }
            if (intencao == Intencao.AGENDAMENTO) {
                return aplicar(agendamentoFlowService.iniciar(telefone, mensagem, ctx), telefone);
            }
            if (intencao == Intencao.CONSULTA_PLANO) {
                return aplicar(consultaPlanoFlowService.iniciar(telefone, mensagem, ctx), telefone);
            }

            log.debug("[CHAT] tel={} intencao={}", telefone, intencao);

            String resposta = chatClient.prompt()
                    .system(montarPromptGeral(ctx))
                    .user(mensagem)
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, telefone))
                    .call()
                    .content();
            return resposta;
        } catch (Exception e) {
            log.error("[CHAT] erro tel={}: {} — {}", telefone, e.getClass().getSimpleName(), e.getMessage(), e);
            return mensagemDeErro(e);
        }
    }

    private Optional<FlowResponse> processarComandoGlobal(String telefone, String mensagem) {
        if (isReset(mensagem)) {
            triagemFlowService.cancelarTriagemAberta(telefone);
            limparSessao(telefone);
            chatMemory.clear(telefone);
            return Optional.of(FlowResponse.texto("Certo, zerei o contexto desta conversa. Como posso ajudar agora?"));
        }
        if (isCancelarFluxo(mensagem)) {
            triagemFlowService.cancelarTriagemAberta(telefone);
            limparSessao(telefone);
            return Optional.of(FlowResponse.texto("Certo, cancelei o fluxo em andamento. Como posso ajudar agora?"));
        }
        if (isFalarComHumano(mensagem)) {
            triagemFlowService.cancelarTriagemAberta(telefone);
            limparSessao(telefone);
            return Optional.of(FlowResponse.texto("No momento o atendimento humano é feito pela clínica. Procure a recepção ou ligue para a clínica se for urgente."));
        }
        return Optional.empty();
    }

    private String aplicar(FlowResponse response, String telefone) {
        if (response == null) {
            return "Desculpe, tive um problema ao processar sua mensagem. Pode tentar novamente?";
        }
        String textoFinal = redigir(response);
        if (response.getTipo() == FlowResponse.Tipo.FINALIZAR_FLUXO) {
            limparSessao(telefone);
            return textoFinal;
        }
        if (response.getTipo() == FlowResponse.Tipo.NOVO_FLUXO) {
            salvarSessao(telefone, response.getNovoFluxo());
            return textoFinal;
        }
        return textoFinal;
    }

    private String redigir(FlowResponse response) {
        if (response.getAto() == null) {
            return response.getTexto();
        }
        return redatorService.redigir(response.getAto());
    }

    private Intencao resolverIntencao(String telefone, String mensagem) {
        Optional<SessaoBotEntity> sessaoOpt = buscarSessaoValida(telefone);
        Intencao ativo = sessaoOpt.map(SessaoBotEntity::getFluxoAtivo).orElse(null);
        boolean mensagemCurta = mensagem.trim().split("\\s+").length <= 5;

        Optional<Intencao> local = classificador.classificarLocalmente(mensagem);
        if (local.isPresent()) {
            Intencao nova = local.get();
            salvarSessao(telefone, nova);
            log.debug("[FLOW] tel={} {} → {} por heurística local", telefone, ativo, nova);
            return nova;
        }

        if (ativo == null && mensagemCurta && classificador.respostaCurtaSemFluxo(mensagem)) {
            log.debug("[FLOW] tel={} GERAL (resposta curta sem fluxo ativo)", telefone);
            return Intencao.GERAL;
        }

        if (ativo == Intencao.TRIAGEM
                && sessaoOpt.map(SessaoBotEntity::getStageAtual)
                        .filter("TRIAGEM_OFERECEU_AGENDAMENTO"::equals)
                        .isPresent()
                && classificador.isAfirmacaoSimples(mensagem)) {
            salvarSessao(telefone, Intencao.AGENDAMENTO);
            log.debug("[FLOW] tel={} TRIAGEM → AGENDAMENTO por aceite de agendamento", telefone);
            return Intencao.AGENDAMENTO;
        }

        if (ativo != null && mensagemCurta) {
            log.debug("[FLOW] tel={} manteve {} (curta, sem classificação)", telefone, ativo);
            salvarSessao(telefone, ativo);
            return ativo;
        }

        IntentResult classificado = classificador.classificar(mensagem, telefone);
        Intencao nova = classificado.getIntencao();
        boolean novaIntencaoClara = nova != Intencao.GERAL && classificado.getConfianca() >= 0.75;
        boolean mudouFluxo = ativo == null || nova != ativo;

        if (novaIntencaoClara && mudouFluxo) {
            salvarSessao(telefone, nova);
            log.debug("[FLOW] tel={} {} → {} (confiança={})", telefone, ativo, nova, classificado.getConfianca());
            return nova;
        }

        if (ativo != null) {
            salvarSessao(telefone, ativo);
            return ativo;
        }

        if (nova != Intencao.GERAL) {
            salvarSessao(telefone, nova);
        }
        return nova;
    }

    private Optional<SessaoBotEntity> buscarSessaoValida(String telefone) {
        Optional<SessaoBotEntity> sessaoOpt = sessaoBotRepository.findById(telefone);
        if (sessaoOpt.isPresent()
                && sessaoOpt.get().getUpdatedAt().isBefore(agora().minusHours(2))) {
            limparSessao(telefone);
            return Optional.empty();
        }
        return sessaoOpt;
    }

    private void salvarSessao(String telefone, Intencao intencao) {
        if (intencao == Intencao.GERAL) return;
        SessaoBotEntity sessao = sessaoBotRepository.findById(telefone).orElseGet(SessaoBotEntity::new);
        boolean mudouFluxo = sessao.getFluxoAtivo() != intencao;

        sessao.setTelefone(telefone);
        sessao.setFluxoAtivo(intencao);
        sessao.setUpdatedAt(agora());
        if (mudouFluxo || sessao.getStageAtual() == null || sessao.getStageAtual().isBlank()) {
            sessao.setStageAtual(stageInicial(intencao));
            sessao.setDadosColetados(null);
            sessao.setAcaoPendente(null);
            sessao.setDadosPendentesJson(null);
        }
        sessaoBotRepository.save(sessao);
    }

    private void limparSessao(String telefone) {
        sessaoBotRepository.findById(telefone).ifPresent(sessaoBotRepository::delete);
    }

    private LocalDateTime agora() {
        return LocalDateTime.now(ZONE_ID);
    }

    private String stageInicial(Intencao intencao) {
        return switch (intencao) {
            case CADASTRO -> CadastroStage.CADASTRO_IDENTIFICANDO_TUTOR.name();
            case AGENDAMENTO -> "AGENDAMENTO_IDENTIFICANDO_ANIMAL";
            case CONSULTA_PLANO -> "CONSULTA_PLANO_IDENTIFICANDO_ANIMAL";
            case TRIAGEM -> "TRIAGEM_IDENTIFICANDO_ANIMAL";
            case GERAL -> null;
        };
    }

    private static boolean isReset(String mensagem) {
        String texto = RespostaParserService.normalizar(mensagem);
        return contemAlgum(texto, "comecar de novo", "começar de novo", "resetar", "zerar");
    }

    private static boolean isCancelarFluxo(String mensagem) {
        String texto = RespostaParserService.normalizar(mensagem);
        return texto.matches("^(cancelar|cancela|cancelar fluxo|sair do fluxo)$");
    }

    private static boolean isFalarComHumano(String mensagem) {
        String texto = RespostaParserService.normalizar(mensagem);
        return contemAlgum(texto, "falar com humano", "atendente humano", "falar com pessoa");
    }

    private static boolean contemAlgum(String texto, String... termos) {
        for (String termo : termos) {
            if (texto.contains(RespostaParserService.normalizar(termo))) return true;
        }
        return false;
    }

    private ConversationContext montarContexto(String telefone, Intencao intencao) {
        ConversationContext ctx = new ConversationContext();
        ctx.setTelefone(telefone);
        ctx.setIntencaoAtual(intencao);
        try {
            petNetApiClient.buscarResponsavelPorTelefone(telefone).ifPresent(r -> {
                ctx.setResponsavelIdentificado(true);
                ctx.setResponsavelId(r.getId());
                ctx.setResponsavelNome(r.getNome());
            });
        } catch (Exception ignored) {
            // .NET indisponível — contexto fica sem responsável identificado
        }
        sessaoBotRepository.findById(telefone).ifPresent(sessao -> {
            ctx.setStageAtual(sessao.getStageAtual());
            ctx.setDadosColetados(sessao.getDadosColetados());
        });
        return ctx;
    }

    private String montarPromptGeral(ConversationContext ctx) {
        String tutorCtx = ctx.isResponsavelIdentificado()
                ? "Tutor identificado: nomeTutor=" + ctx.getResponsavelNome()
                : "Tutor não identificado.";
        return GERAL_SYSTEM + "\nCONTEXTO: " + tutorCtx;
    }

    private String mensagemDeErro(Exception e) {
        String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        Throwable causa = e.getCause() != null ? e.getCause() : e;
        String causaMsg = causa.getMessage() != null ? causa.getMessage().toLowerCase() : "";

        if (msg.contains("429") || causaMsg.contains("429")
                || msg.contains("rate limit") || msg.contains("quota")
                || msg.contains("resource_exhausted") || causaMsg.contains("too many requests")) {
            return "Estou com muitas conversas agora 😅 Aguarde alguns segundos e tente novamente!";
        }
        if (msg.contains("timeout") || causaMsg.contains("timeout")
                || msg.contains("timed out") || msg.contains("connect")) {
            return "A resposta demorou mais do que esperado 🕐 Pode tentar novamente?";
        }
        return "Desculpe, tive um problema ao processar sua mensagem. Pode tentar novamente? 🙏";
    }
}
