package br.com.fiap.petbuddies.flow;

import br.com.fiap.petbuddies.client.PetNetApiClient;
import br.com.fiap.petbuddies.domain.entity.SessaoBotEntity;
import br.com.fiap.petbuddies.domain.enums.AcaoPendente;
import br.com.fiap.petbuddies.domain.enums.Intencao;
import br.com.fiap.petbuddies.domain.repository.SessaoBotRepository;
import br.com.fiap.petbuddies.dto.bot.AtoComunicativo;
import br.com.fiap.petbuddies.dto.bot.ConversationContext;
import br.com.fiap.petbuddies.dto.client.AgendarConsultaRequest;
import br.com.fiap.petbuddies.dto.client.AnimalDto;
import br.com.fiap.petbuddies.dto.client.ConsultaDto;
import br.com.fiap.petbuddies.dto.client.JanelaAtendimentoDto;
import br.com.fiap.petbuddies.exception.PetNetApiUnavailableException;
import br.com.fiap.petbuddies.flow.dto.AnimalResumo;
import br.com.fiap.petbuddies.flow.dto.DadosAgendamentoPendente;
import br.com.fiap.petbuddies.flow.dto.DadosCadastroPendente;
import br.com.fiap.petbuddies.flow.dto.FlowResponse;
import br.com.fiap.petbuddies.flow.dto.JanelaOfertada;
import br.com.fiap.petbuddies.service.bot.RespostaParserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
public class AgendamentoFlowService {

    private static final Logger log = LoggerFactory.getLogger(AgendamentoFlowService.class);
    private static final ZoneId ZONE_ID = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    private final PetNetApiClient petNetApiClient;
    private final SessaoBotRepository sessaoBotRepository;
    private final FlowSessaoHelper sessaoHelper;
    private final RespostaParserService parserService;

    public AgendamentoFlowService(PetNetApiClient petNetApiClient,
                                  SessaoBotRepository sessaoBotRepository,
                                  FlowSessaoHelper sessaoHelper,
                                  RespostaParserService parserService) {
        this.petNetApiClient = petNetApiClient;
        this.sessaoBotRepository = sessaoBotRepository;
        this.sessaoHelper = sessaoHelper;
        this.parserService = parserService;
    }

    @Transactional
    public FlowResponse iniciar(String telefone, String mensagem, ConversationContext ctx) {
        if (!ctx.isResponsavelIdentificado() || ctx.getResponsavelId() == null) {
            return prepararCadastroAntesDeAgendar(telefone, null, null);
        }

        List<AnimalDto> animais = petNetApiClient.listarAnimaisDoResponsavel(ctx.getResponsavelId());
        if (animais.isEmpty()) {
            return prepararCadastroAntesDeAgendar(telefone, ctx.getResponsavelId(), ctx.getResponsavelNome());
        }

        if (isCancelamento(mensagem)) {
            return processarCancelamentoSimples(telefone, mensagem, animais);
        }

        if (isListagemConsultas(mensagem)) {
            return listarConsultasFuturas(animais);
        }

        Optional<AnimalDto> animal = FlowSupport.resolverAnimal(animais, mensagem);
        if (animal.isEmpty()) {
            salvarEscolhaAnimal(telefone, ctx.getResponsavelId(), animais);
            String fallback = "Para qual animal você quer marcar a consulta? " + FlowSupport.formatarAnimais(animais);
            return FlowResponse.comAto(AtoComunicativo.perguntar(fallback,
                    FlowSupport.dados("pergunta", "Para qual animal você quer marcar a consulta?",
                            "animaisCandidatos", FlowSupport.formatarAnimais(animais))));
        }
        return iniciarAgendamento(telefone, animal.get().getId(), animal.get().getNome());
    }

    @Transactional
    public FlowResponse iniciarAgendamento(String telefone, Long animalId, String animalNome) {
        List<JanelaAtendimentoDto> todasDisponiveis = petNetApiClient.listarJanelasDisponiveis().stream()
                .sorted(Comparator.comparing(JanelaAtendimentoDto::getDataHoraInicio,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        if (todasDisponiveis.isEmpty()) {
            limparPendencia(telefone);
            String fallback = "No momento não encontrei horários livres para consulta. Tente novamente mais tarde.";
            return FlowResponse.finalizarFluxoComAto(AtoComunicativo.orientar(fallback,
                    FlowSupport.dados("motivo", "sem_janelas_disponiveis")));
        }

        DadosAgendamentoPendente dados = new DadosAgendamentoPendente();
        dados.setAnimalId(animalId);
        dados.setAnimalNome(animalNome);

        if (todasDisponiveis.size() > 5) {
            dados.setTodasJanelas(toJanelasOfertadas(todasDisponiveis));
            salvarSessaoAgendamento(telefone, AcaoPendente.AGENDAMENTO_AGUARDANDO_TURNO, dados);
            String fallback = "Encontrei vários horários disponíveis para " + FlowSupport.nomeOuAnimal(animalNome)
                    + ". Você prefere manhã, tarde ou noite?";
            return FlowResponse.comAto(AtoComunicativo.perguntar(fallback,
                    FlowSupport.dados("animalNome", FlowSupport.nomeOuAnimal(animalNome),
                            "pergunta", "Manhã, tarde ou noite?", "totalJanelas", todasDisponiveis.size())));
        }

        dados.setJanelasOfertadas(toJanelasOfertadas(todasDisponiveis));
        salvarSessaoAgendamento(telefone, AcaoPendente.AGENDAMENTO_AGUARDANDO_ESCOLHA_JANELA, dados);
        String fallback = "Encontrei estes horários para " + FlowSupport.nomeOuAnimal(animalNome) + ":\n"
                + formatarJanelas(dados.getJanelasOfertadas()) + "\nQual opção você prefere?";
        return FlowResponse.comAto(AtoComunicativo.apresentarOpcoes(fallback,
                FlowSupport.dados("animalNome", FlowSupport.nomeOuAnimal(animalNome),
                        "janelas", formatarJanelasLista(dados.getJanelasOfertadas()),
                        "pergunta", "Qual opção você prefere?")));
    }

    @Transactional
    public FlowResponse processar(String telefone, String mensagem) {
        SessaoBotEntity sessao = sessaoBotRepository.findById(telefone).orElse(null);
        if (sessao == null || sessao.getAcaoPendente() == null) {
            String fallback = "Não encontrei um agendamento em andamento. Me diga novamente que consulta você quer marcar.";
            return FlowResponse.finalizarFluxoComAto(AtoComunicativo.orientar(fallback,
                    FlowSupport.dados("motivo", "agendamento_nao_encontrado")));
        }

        return switch (sessao.getAcaoPendente()) {
            case AGENDAMENTO_AGUARDANDO_ESCOLHA_ANIMAL -> selecionarAnimal(telefone, mensagem, lerDados(sessao));
            case AGENDAMENTO_AGUARDANDO_TURNO -> processarEscolhaTurno(telefone, mensagem);
            case AGENDAMENTO_AGUARDANDO_ESCOLHA_JANELA -> selecionarJanela(telefone, mensagem);
            case AGENDAMENTO_AGUARDANDO_CONFIRMACAO -> confirmarAgendamento(telefone, mensagem);
        };
    }

    @Transactional
    public FlowResponse processarEscolhaTurno(String telefone, String mensagem) {
        SessaoBotEntity sessao = sessaoBotRepository.findById(telefone).orElse(null);
        if (sessao == null) {
            String fallback = "Não encontrei o agendamento em andamento. Vamos começar de novo?";
            return FlowResponse.finalizarFluxoComAto(AtoComunicativo.orientar(fallback,
                    FlowSupport.dados("motivo", "sessao_nao_encontrada")));
        }

        DadosAgendamentoPendente dados = lerDados(sessao);
        Optional<String> turno = parserService.interpretarTurno(mensagem);

        List<JanelaOfertada> candidatas = turno.isPresent()
                ? filtrarPorTurno(dados.getTodasJanelas(), turno.get())
                : List.of();

        if (candidatas.isEmpty()) {
            if (turno.isEmpty()) {
                String fallback = "Não entendi a preferência. Você quer manhã, tarde ou noite?";
                return FlowResponse.comAto(AtoComunicativo.perguntar(fallback,
                        FlowSupport.dados("pergunta", "Manhã, tarde ou noite?", "turnosDisponiveis", "manhã, tarde, noite")));
            }
            List<JanelaOfertada> fallbackJanelas = renumerar(dados.getTodasJanelas().stream().limit(5).toList());
            String fallback = "Não encontrei horários no turno da " + turno.get().toLowerCase()
                    + ". Veja os mais próximos disponíveis:\n" + formatarJanelas(fallbackJanelas) + "\nQual opção você prefere?";
            dados.setJanelasOfertadas(fallbackJanelas);
            salvarSessaoAgendamento(telefone, AcaoPendente.AGENDAMENTO_AGUARDANDO_ESCOLHA_JANELA, dados);
            return FlowResponse.comAto(AtoComunicativo.apresentarOpcoes(fallback,
                    FlowSupport.dados("animalNome", FlowSupport.nomeOuAnimal(dados.getAnimalNome()),
                            "janelas", formatarJanelasLista(fallbackJanelas), "pergunta", "Qual opção você prefere?")));
        }

        List<JanelaOfertada> ofertadas = renumerar(candidatas.stream().limit(5).toList());
        dados.setJanelasOfertadas(ofertadas);
        salvarSessaoAgendamento(telefone, AcaoPendente.AGENDAMENTO_AGUARDANDO_ESCOLHA_JANELA, dados);
        String fallback = "Horários disponíveis no turno da " + turno.get().toLowerCase() + " para "
                + FlowSupport.nomeOuAnimal(dados.getAnimalNome()) + ":\n"
                + formatarJanelas(ofertadas) + "\nQual opção você prefere?";
        return FlowResponse.comAto(AtoComunicativo.apresentarOpcoes(fallback,
                FlowSupport.dados("animalNome", FlowSupport.nomeOuAnimal(dados.getAnimalNome()),
                        "janelas", formatarJanelasLista(ofertadas),
                        "turno", turno.get(), "pergunta", "Qual opção você prefere?")));
    }

    @Transactional
    public FlowResponse selecionarJanela(String telefone, String escolhaTexto) {
        SessaoBotEntity sessao = sessaoBotRepository.findById(telefone).orElse(null);
        if (sessao == null) {
            String fallback = "Não encontrei os horários ofertados. Vamos começar o agendamento de novo?";
            return FlowResponse.finalizarFluxoComAto(AtoComunicativo.orientar(fallback,
                    FlowSupport.dados("motivo", "janelas_nao_encontradas")));
        }

        DadosAgendamentoPendente dados = lerDados(sessao);
        Optional<JanelaOfertada> escolhida = resolverJanela(dados.getJanelasOfertadas(), escolhaTexto);
        if (escolhida.isEmpty()) {
            String fallback = "Não consegui identificar essa opção. Escolha pelo número da lista:\n"
                    + formatarJanelas(dados.getJanelasOfertadas());
            return FlowResponse.comAto(AtoComunicativo.perguntar(fallback,
                    FlowSupport.dados("motivo", "opcao_nao_identificada",
                            "janelas", formatarJanelasLista(dados.getJanelasOfertadas()))));
        }

        dados.setJanelaEscolhida(escolhida.get());
        salvarSessaoAgendamento(telefone, AcaoPendente.AGENDAMENTO_AGUARDANDO_CONFIRMACAO, dados);
        String fallback = "Perfeito. Confirmo a consulta de " + FlowSupport.nomeOuAnimal(dados.getAnimalNome())
                + " em " + formatarJanela(escolhida.get()) + "?";
        return FlowResponse.comAto(AtoComunicativo.confirmar(fallback,
                FlowSupport.dados("animalNome", FlowSupport.nomeOuAnimal(dados.getAnimalNome()),
                        "dataHora", formatarJanela(escolhida.get()),
                        "veterinario", escolhida.get().getVeterinarioNome())));
    }

    @Transactional
    public FlowResponse confirmarAgendamento(String telefone, String mensagem) {
        if (parserService.isNegacao(mensagem)) {
            limparPendencia(telefone);
            String fallback = "Tudo bem, não vou agendar agora.";
            return FlowResponse.finalizarFluxoComAto(AtoComunicativo.orientar(fallback,
                    FlowSupport.dados("status", "agendamento_cancelado")));
        }
        if (!parserService.isConfirmacao(mensagem)) {
            String fallback = "Só preciso da confirmação para criar a consulta. Pode responder com sim ou não.";
            return FlowResponse.comAto(AtoComunicativo.confirmar(fallback,
                    FlowSupport.dados("pergunta", "Confirma a criação da consulta?", "respostasEsperadas", "sim ou não")));
        }

        SessaoBotEntity sessao = sessaoBotRepository.findById(telefone).orElse(null);
        if (sessao == null) {
            String fallback = "Não encontrei o agendamento em andamento. Vamos começar de novo?";
            return FlowResponse.finalizarFluxoComAto(AtoComunicativo.orientar(fallback,
                    FlowSupport.dados("motivo", "agendamento_nao_encontrado")));
        }

        DadosAgendamentoPendente dados = lerDados(sessao);
        if (dados.getAnimalId() == null || dados.getJanelaEscolhida() == null) {
            limparPendencia(telefone);
            String fallback = "Perdi os dados do horário escolhido. Vamos começar o agendamento de novo?";
            return FlowResponse.finalizarFluxoComAto(AtoComunicativo.orientar(fallback,
                    FlowSupport.dados("motivo", "dados_agendamento_incompletos")));
        }

        try {
            ConsultaDto consulta = petNetApiClient.agendarConsulta(new AgendarConsultaRequest(
                    dados.getAnimalId(),
                    dados.getJanelaEscolhida().getJanelaId(),
                    "ROTINA"));
            limparPendencia(telefone);
            String fallback = "Consulta agendada para "
                    + DATA_HORA.format(consulta.getDataHora())
                    + ". Status: " + consulta.getStatus() + ".";
            return FlowResponse.finalizarFluxoComAto(AtoComunicativo.confirmarSucesso(fallback,
                    FlowSupport.dados("consultaId", consulta.getId(),
                            "dataHora", DATA_HORA.format(consulta.getDataHora()),
                            "status", consulta.getStatus())));
        } catch (PetNetApiUnavailableException e) {
            log.warn("[AGENDAMENTO] falha ao confirmar tel={}: {}", telefone, e.getMessage());
            String fallback = "Não consegui criar a consulta agora. Pode tentar confirmar novamente em instantes?";
            return FlowResponse.comAto(AtoComunicativo.orientar(fallback,
                    FlowSupport.dados("motivo", "petnetapi_indisponivel")));
        }
    }

    @Transactional
    public Optional<FlowResponse> retomarAposCadastro(String telefone, Long animalId, String animalNome) {
        SessaoBotEntity sessao = sessaoBotRepository.findById(telefone).orElse(null);
        if (sessao == null) return Optional.empty();

        DadosAgendamentoPendente dados = lerDados(sessao);
        if (dados.getFluxoRetorno() != Intencao.AGENDAMENTO) {
            return Optional.empty();
        }
        return Optional.of(iniciarAgendamento(telefone, animalId, animalNome));
    }

    private FlowResponse prepararCadastroAntesDeAgendar(String telefone, Long responsavelId, String responsavelNome) {
        DadosCadastroPendente dados = new DadosCadastroPendente();
        dados.setResponsavelId(responsavelId);
        dados.setNomeTutor(responsavelNome);
        dados.setFluxoRetorno(Intencao.AGENDAMENTO);

        SessaoBotEntity sessao = sessaoBotRepository.findById(telefone).orElseGet(SessaoBotEntity::new);
        sessao.setTelefone(telefone);
        sessao.setFluxoAtivo(Intencao.CADASTRO);
        sessao.setStageAtual(responsavelId == null ? "CADASTRO_IDENTIFICANDO_TUTOR" : "CADASTRO_COLETANDO_ANIMAL");
        sessao.setAcaoPendente(null);
        sessao.setDadosPendentesJson(sessaoHelper.toJson(dados));
        sessao.setUpdatedAt(LocalDateTime.now(ZONE_ID));
        sessaoBotRepository.save(sessao);

        if (responsavelId == null) {
            String fallback = "Para marcar a consulta, preciso cadastrar o tutor e o animal primeiro. Me diga o nome completo do tutor.";
            return FlowResponse.comAto(AtoComunicativo.orientar(fallback,
                    FlowSupport.dados("motivo", "cadastro_necessario", "pergunta", "Me diga o nome completo do tutor.")));
        }

        String fallback = "Para marcar a consulta, preciso cadastrar o animal primeiro. Me diga os dados dele.";
        return FlowResponse.comAto(AtoComunicativo.perguntar(fallback,
                FlowSupport.dados("motivo", "animal_nao_cadastrado", "responsavelNome", responsavelNome,
                        "pergunta", "Me diga os dados do animal.")));
    }

    private FlowResponse selecionarAnimal(String telefone, String mensagem, DadosAgendamentoPendente dados) {
        Optional<AnimalResumo> animal = FlowSupport.resolverAnimalResumo(dados.getAnimaisCandidatos(), mensagem, parserService);
        if (animal.isEmpty()) {
            String fallback = "Não consegui identificar qual animal. Escolha pelo nome ou número: "
                    + FlowSupport.formatarAnimaisResumo(dados.getAnimaisCandidatos());
            return FlowResponse.comAto(AtoComunicativo.perguntar(fallback,
                    FlowSupport.dados("motivo", "animal_nao_identificado",
                            "animaisCandidatos", FlowSupport.formatarAnimaisResumo(dados.getAnimaisCandidatos()))));
        }
        return iniciarAgendamento(telefone, animal.get().getAnimalId(), animal.get().getNome());
    }

    private void salvarEscolhaAnimal(String telefone, Long responsavelId, List<AnimalDto> animais) {
        DadosAgendamentoPendente dados = new DadosAgendamentoPendente();
        dados.setResponsavelId(responsavelId);
        dados.setAnimaisCandidatos(FlowSupport.toAnimaisResumo(animais));
        salvarSessaoAgendamento(telefone, AcaoPendente.AGENDAMENTO_AGUARDANDO_ESCOLHA_ANIMAL, dados);
    }

    private void salvarSessaoAgendamento(String telefone, AcaoPendente acao, DadosAgendamentoPendente dados) {
        SessaoBotEntity sessao = sessaoBotRepository.findById(telefone).orElseGet(SessaoBotEntity::new);
        sessao.setTelefone(telefone);
        sessao.setFluxoAtivo(Intencao.AGENDAMENTO);
        sessao.setStageAtual(acao.name());
        sessao.setAcaoPendente(acao);
        sessao.setDadosPendentesJson(sessaoHelper.toJson(dados));
        sessao.setUpdatedAt(LocalDateTime.now(ZONE_ID));
        sessaoBotRepository.save(sessao);
    }

    private void limparPendencia(String telefone) {
        sessaoBotRepository.findById(telefone).ifPresent(sessao -> {
            sessao.setAcaoPendente(null);
            sessao.setDadosPendentesJson(null);
            sessao.setUpdatedAt(LocalDateTime.now(ZONE_ID));
            sessaoBotRepository.save(sessao);
        });
    }

    private DadosAgendamentoPendente lerDados(SessaoBotEntity sessao) {
        DadosAgendamentoPendente resultado = sessaoHelper.lerDados(sessao.getDadosPendentesJson(), DadosAgendamentoPendente.class);
        if (resultado == null) {
            log.warn("[AGENDAMENTO] dadosPendentesJson inválido tel={}", sessao.getTelefone());
            return new DadosAgendamentoPendente();
        }
        return resultado;
    }

    private Optional<JanelaOfertada> resolverJanela(List<JanelaOfertada> janelas, String mensagem) {
        Optional<Integer> ordem = parserService.interpretarOrdem(mensagem);
        if (ordem.isPresent()) {
            return janelas.stream().filter(j -> ordem.get().equals(j.getOrdem())).findFirst();
        }
        Optional<Integer> hora = parserService.interpretarHora(mensagem);
        if (hora.isPresent()) {
            return janelas.stream()
                    .filter(j -> j.getDataHoraInicio() != null && j.getDataHoraInicio().getHour() == hora.get())
                    .findFirst();
        }
        return Optional.empty();
    }

    private List<JanelaOfertada> toJanelasOfertadas(List<JanelaAtendimentoDto> janelas) {
        List<JanelaOfertada> result = new ArrayList<>();
        int ordem = 1;
        for (JanelaAtendimentoDto janela : janelas) {
            JanelaOfertada ofertada = new JanelaOfertada();
            ofertada.setOrdem(ordem++);
            ofertada.setJanelaId(janela.getId());
            ofertada.setDataHoraInicio(janela.getDataHoraInicio());
            ofertada.setDataHoraFim(janela.getDataHoraFim());
            ofertada.setVeterinarioNome(janela.getVeterinarioNome());
            result.add(ofertada);
        }
        return result;
    }

    private FlowResponse processarCancelamentoSimples(String telefone, String mensagem, List<AnimalDto> animais) {
        Optional<AnimalDto> animal = FlowSupport.resolverAnimal(animais, mensagem);
        if (animal.isEmpty()) {
            salvarEscolhaAnimal(telefone, null, animais);
            String fallback = "Para cancelar, preciso saber de qual animal é a consulta. " + FlowSupport.formatarAnimais(animais);
            return FlowResponse.comAto(AtoComunicativo.perguntar(fallback,
                    FlowSupport.dados("pergunta", "De qual animal é a consulta?",
                            "animaisCandidatos", FlowSupport.formatarAnimais(animais))));
        }
        List<ConsultaDto> consultas = petNetApiClient.listarConsultasDoAnimal(animal.get().getId());
        if (consultas.isEmpty()) {
            String fallback = "Não encontrei consulta agendada para " + FlowSupport.nomeOuAnimal(animal.get().getNome()) + ".";
            return FlowResponse.finalizarFluxoComAto(AtoComunicativo.informarResultado(fallback,
                    FlowSupport.dados("animalNome", FlowSupport.nomeOuAnimal(animal.get().getNome()), "consultasEncontradas", 0)));
        }
        String fallback = "Encontrei consulta para " + FlowSupport.nomeOuAnimal(animal.get().getNome())
                + ", mas o cancelamento determinístico fica fora deste corte. Posso ajudar a marcar uma nova consulta.";
        return FlowResponse.finalizarFluxoComAto(AtoComunicativo.orientar(fallback,
                FlowSupport.dados("animalNome", FlowSupport.nomeOuAnimal(animal.get().getNome()), "cancelamentoDisponivel", false)));
    }

    private static List<JanelaOfertada> renumerar(List<JanelaOfertada> janelas) {
        for (int i = 0; i < janelas.size(); i++) {
            janelas.get(i).setOrdem(i + 1);
        }
        return janelas;
    }

    private static List<JanelaOfertada> filtrarPorTurno(List<JanelaOfertada> janelas, String turno) {
        return janelas.stream()
                .filter(j -> j.getDataHoraInicio() != null && turnoDeHora(j.getDataHoraInicio().getHour()).equals(turno))
                .toList();
    }

    private static String turnoDeHora(int hora) {
        if (hora < 12) return "MANHA";
        if (hora < 18) return "TARDE";
        return "NOITE";
    }

    private static boolean isCancelamento(String mensagem) {
        String texto = RespostaParserService.normalizar(mensagem);
        return texto.contains("cancelar") || texto.contains("desmarcar");
    }

    private static boolean isListagemConsultas(String mensagem) {
        String texto = RespostaParserService.normalizar(mensagem);
        return texto.contains("minhas consultas") || texto.contains("minha consulta")
                || texto.contains("proxima consulta") || texto.contains("tenho consulta")
                || texto.contains("consulta marcada") || texto.contains("consultas marcadas")
                || texto.contains("ver consulta")
                || (texto.contains("quando") && texto.contains("consulta"));
    }

    private FlowResponse listarConsultasFuturas(List<AnimalDto> animais) {
        LocalDateTime agora = LocalDateTime.now(ZONE_ID);
        Map<Long, String> nomesPorAnimal = animais.stream()
                .collect(Collectors.toMap(AnimalDto::getId, AnimalDto::getNome));

        List<ConsultaDto> futuras = animais.stream()
                .flatMap(a -> petNetApiClient.listarConsultasDoAnimal(a.getId()).stream())
                .filter(c -> "AGENDADA".equals(c.getStatus()) || "CONFIRMADA".equals(c.getStatus()))
                .filter(c -> c.getDataHora() != null && c.getDataHora().isAfter(agora))
                .sorted(Comparator.comparing(ConsultaDto::getDataHora))
                .limit(3)
                .toList();

        if (futuras.isEmpty()) {
            String fallback = "Não encontrei consultas marcadas para seus animais.";
            return FlowResponse.finalizarFluxoComAto(AtoComunicativo.informarResultado(fallback, FlowSupport.dados("motivo", "sem_consultas_futuras")));
        }

        List<Map<String, Object>> consultasLista = new ArrayList<>();
        for (ConsultaDto c : futuras) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("animalNome", nomesPorAnimal.getOrDefault(c.getAnimalId(), "animal"));
            item.put("dataHora", DATA_HORA.format(c.getDataHora()));
            item.put("status", c.getStatus());
            consultasLista.add(item);
        }

        String fallback = "Suas próximas consultas: " + futuras.stream()
                .map(c -> nomesPorAnimal.getOrDefault(c.getAnimalId(), "animal") + " em " + DATA_HORA.format(c.getDataHora()))
                .collect(Collectors.joining("; "));

        return FlowResponse.finalizarFluxoComAto(AtoComunicativo.informarResultado(fallback,
                FlowSupport.dados("consultas", consultasLista, "total", futuras.size())));
    }

    private static String formatarJanelas(List<JanelaOfertada> janelas) {
        return janelas.stream()
                .map(j -> j.getOrdem() + ". " + formatarJanela(j))
                .collect(Collectors.joining("\n"));
    }

    private static List<String> formatarJanelasLista(List<JanelaOfertada> janelas) {
        return janelas.stream()
                .map(j -> j.getOrdem() + ". " + formatarJanela(j))
                .collect(Collectors.toList());
    }

    private static String formatarJanela(JanelaOfertada janela) {
        String inicio = janela.getDataHoraInicio() == null ? "horário a confirmar" : DATA_HORA.format(janela.getDataHoraInicio());
        String fim = janela.getDataHoraFim() == null ? "" : " até " + DATA_HORA.format(janela.getDataHoraFim());
        String vet = janela.getVeterinarioNome() == null || janela.getVeterinarioNome().isBlank()
                ? "veterinário da clínica" : janela.getVeterinarioNome();
        return inicio + fim + " com " + vet;
    }
}
