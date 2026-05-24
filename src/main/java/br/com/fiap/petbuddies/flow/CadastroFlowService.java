package br.com.fiap.petbuddies.flow;

import br.com.fiap.petbuddies.client.PetNetApiClient;
import br.com.fiap.petbuddies.domain.entity.SessaoBotEntity;
import br.com.fiap.petbuddies.domain.enums.CadastroStage;
import br.com.fiap.petbuddies.domain.enums.Intencao;
import br.com.fiap.petbuddies.domain.repository.SessaoBotRepository;
import br.com.fiap.petbuddies.dto.bot.AtoComunicativo;
import br.com.fiap.petbuddies.dto.bot.ConversationContext;
import br.com.fiap.petbuddies.dto.client.AnimalDto;
import br.com.fiap.petbuddies.dto.client.CadastrarAnimalRequest;
import br.com.fiap.petbuddies.dto.client.CadastrarResponsavelRequest;
import br.com.fiap.petbuddies.dto.client.ResponsavelDto;
import br.com.fiap.petbuddies.exception.PetNetApiConflictException;
import br.com.fiap.petbuddies.exception.PetNetApiUnavailableException;
import br.com.fiap.petbuddies.flow.dto.DadosAgendamentoPendente;
import br.com.fiap.petbuddies.flow.dto.DadosCadastroPendente;
import br.com.fiap.petbuddies.flow.dto.FlowResponse;
import br.com.fiap.petbuddies.service.bot.RespostaParserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CadastroFlowService {

    private enum CampoCadastro {
        NOME_ANIMAL, ESPECIE, PORTE, SEXO, CASTRADO, DATA_NASCIMENTO
    }

    private static final Logger log = LoggerFactory.getLogger(CadastroFlowService.class);
    private static final ZoneId ZONE_ID = ZoneId.of("America/Sao_Paulo");
    private static final Pattern NOME_ANIMAL_APOS_ESPECIE = Pattern.compile(
            "(?i)(?:meu|minha|o|a)?\\s*(?:cachorro|cachorra|cão|cao|gato|gata|coelho|coelha|hamster|p[aá]ssaro|ave)\\s+([\\p{L}]{2,30})\\b");
    private static final Pattern NOME_ANIMAL_EXPLICITO = Pattern.compile(
            "(?i)(?:se chama|chama|chamado|chamada|nome (?:dele|dela|do animal)?\\s*(?:é|e)?)\\s+([\\p{L}]{2,30})\\b");

    private final PetNetApiClient petNetApiClient;
    private final SessaoBotRepository sessaoBotRepository;
    private final RespostaParserService parserService;
    private final FlowSessaoHelper sessaoHelper;
    private final AgendamentoFlowService agendamentoFlowService;

    public CadastroFlowService(PetNetApiClient petNetApiClient, SessaoBotRepository sessaoBotRepository, RespostaParserService parserService, FlowSessaoHelper sessaoHelper,AgendamentoFlowService agendamentoFlowService) {
        this.petNetApiClient = petNetApiClient;
        this.sessaoBotRepository = sessaoBotRepository;
        this.parserService = parserService;
        this.sessaoHelper = sessaoHelper;
        this.agendamentoFlowService = agendamentoFlowService;
    }

    @Transactional
    public FlowResponse iniciar(String telefone, String mensagem, ConversationContext ctx) {
        DadosCadastroPendente dados = new DadosCadastroPendente();
        dados.setFluxoRetorno(fluxoRetornoAtual(telefone).orElse(null));
        aplicarExtracaoAnimal(dados, mensagem);

        if (ctx.isResponsavelIdentificado() && ctx.getResponsavelId() != null) {
            dados.setResponsavelId(ctx.getResponsavelId());
            dados.setNomeTutor(ctx.getResponsavelNome());
            salvarSessaoCadastro(telefone, CadastroStage.CADASTRO_COLETANDO_ANIMAL, dados);
            return proximaPerguntaAnimal(telefone, dados);
        }

        extrairNomeTutorExplicito(mensagem).ifPresent(dados::setNomeTutor);
        if (isBlank(dados.getNomeTutor())) {
            salvarSessaoCadastro(telefone, CadastroStage.CADASTRO_AGUARDANDO_NOME_TUTOR, dados);
            String fallback = "Para começar o cadastro, me diga o nome completo do tutor.";
            return FlowResponse.comAto(AtoComunicativo.perguntar(fallback,
                    FlowSupport.dados("pergunta", "Nome completo do tutor")));
        }

        salvarSessaoCadastro(telefone, CadastroStage.CADASTRO_COLETANDO_ANIMAL, dados);
        return proximaPerguntaAnimal(telefone, dados);
    }

    @Transactional
    public FlowResponse processar(String telefone, String mensagem) {
        SessaoBotEntity sessao = sessaoBotRepository.findById(telefone).orElse(null);
        if (sessao == null) {
            String fallback = "Não encontrei um cadastro em andamento. Me diga novamente que cadastro você quer fazer.";
            return FlowResponse.finalizarFluxoComAto(AtoComunicativo.orientar(fallback,
                    FlowSupport.dados("motivo", "cadastro_nao_encontrado")));
        }

        DadosCadastroPendente dados = lerDadosCadastro(sessao);
        CadastroStage stage = stageAtual(sessao, dados);

        if (stage == CadastroStage.CADASTRO_IDENTIFICANDO_TUTOR
                || stage == CadastroStage.CADASTRO_AGUARDANDO_NOME_TUTOR) {
            return processarNomeTutor(telefone, mensagem, dados);
        }
        if (stage == CadastroStage.CADASTRO_AGUARDANDO_CONFIRMACAO) {
            return processarConfirmacao(telefone, mensagem, dados);
        }
        if (stage == CadastroStage.CADASTRO_COLETANDO_ANIMAL) {
            return processarDadosAnimal(telefone, mensagem, dados);
        }

        String fallback = "Esse cadastro já foi finalizado. Me diga como posso ajudar agora.";
        return FlowResponse.finalizarFluxoComAto(AtoComunicativo.orientar(fallback,
                FlowSupport.dados("motivo", "cadastro_finalizado")));
    }

    private FlowResponse processarNomeTutor(String telefone, String mensagem, DadosCadastroPendente dados) {
        String nome = limparNome(mensagem);
        if (nome.length() < 3) {
            String fallback = "Me diga o nome completo do tutor para seguir com o cadastro.";
            salvarSessaoCadastro(telefone, CadastroStage.CADASTRO_AGUARDANDO_NOME_TUTOR, dados);
            return FlowResponse.comAto(AtoComunicativo.perguntar(fallback,
                    FlowSupport.dados("pergunta", "Nome completo do tutor")));
        }

        dados.setNomeTutor(nome);
        salvarSessaoCadastro(telefone, CadastroStage.CADASTRO_COLETANDO_ANIMAL, dados);
        return proximaPerguntaAnimal(telefone, dados);
    }

    private FlowResponse processarDadosAnimal(String telefone, String mensagem, DadosCadastroPendente dados) {
        CampoCadastro campoAntes = proximoCampo(dados).orElse(null);
        aplicarExtracaoAnimal(dados, mensagem);

        if (campoAntes == CampoCadastro.NOME_ANIMAL && isBlank(dados.getNomeAnimal()) && !pareceDadoDeAnimal(mensagem)) {
            dados.setNomeAnimal(limparNome(mensagem));
        }

        inferirPorteSeSeguro(dados);
        salvarSessaoCadastro(telefone, CadastroStage.CADASTRO_COLETANDO_ANIMAL, dados);
        return proximaPerguntaAnimal(telefone, dados);
    }

    private FlowResponse processarConfirmacao(String telefone, String mensagem, DadosCadastroPendente dados) {
        if (parserService.isConfirmacao(mensagem)) {
            return persistirCadastro(telefone, dados);
        }

        if (parserService.isNegacao(mensagem)) {
            aplicarExtracaoAnimal(dados, mensagem);
            salvarSessaoCadastro(telefone, CadastroStage.CADASTRO_COLETANDO_ANIMAL, dados);
            if (cadastroCompleto(dados) && !mensagem.trim().equalsIgnoreCase("não") && !mensagem.trim().equalsIgnoreCase("nao")) {
                return pedirConfirmacao(telefone, dados);
            }
            String fallback = "Tudo bem. Me envie o dado corrigido do animal para eu ajustar o cadastro.";
            return FlowResponse.comAto(AtoComunicativo.perguntar(fallback,
                    FlowSupport.dados("pergunta", "Dado corrigido do animal", "resumoAtual", resumoDados(dados))));
        }

        aplicarExtracaoAnimal(dados, mensagem);
        if (cadastroCompleto(dados)) {
            return pedirConfirmacao(telefone, dados);
        }

        return proximaPerguntaAnimal(telefone, dados);
    }

    private FlowResponse proximaPerguntaAnimal(String telefone, DadosCadastroPendente dados) {
        inferirPorteSeSeguro(dados);
        Optional<CampoCadastro> campo = proximoCampo(dados);
        if (campo.isEmpty()) {
            return pedirConfirmacao(telefone, dados);
        }

        salvarSessaoCadastro(telefone, CadastroStage.CADASTRO_COLETANDO_ANIMAL, dados);
        String fallback = perguntaPara(campo.get(), dados);
        return FlowResponse.comAto(AtoComunicativo.perguntar(fallback,
                FlowSupport.dados("campo", campo.get().name(), "pergunta", fallback, "resumoAtual", resumoDados(dados))));
    }

    private FlowResponse pedirConfirmacao(String telefone, DadosCadastroPendente dados) {
        salvarSessaoCadastro(telefone, CadastroStage.CADASTRO_AGUARDANDO_CONFIRMACAO, dados);
        String fallback = "Confirma os dados do cadastro?\n" + resumoDados(dados);
        return FlowResponse.comAto(AtoComunicativo.confirmar(fallback, dadosResumo(dados)));
    }

    private FlowResponse persistirCadastro(String telefone, DadosCadastroPendente dados) {
        try {
            Long responsavelId = dados.getResponsavelId();
            if (responsavelId == null) {
                responsavelId = cadastrarOuRecuperarResponsavel(telefone, dados);
            }

            AnimalDto animal = petNetApiClient.cadastrarAnimal(new CadastrarAnimalRequest(
                    responsavelId,
                    dados.getNomeAnimal(),
                    dados.getEspecie(),
                    dados.getPorte(),
                    dados.getSexo(),
                    Boolean.TRUE.equals(dados.getCastrado()),
                    dados.getDataNascimento()));

            if (dados.getFluxoRetorno() == Intencao.AGENDAMENTO) {
                prepararRetornoAgendamento(telefone);
                Optional<FlowResponse> retomada = agendamentoFlowService.retomarAposCadastro(telefone, animal.getId(), animal.getNome());
                if (retomada.isPresent()) {
                    return retomada.get();
                }
                return agendamentoFlowService.iniciarAgendamento(telefone, animal.getId(), animal.getNome());
            }

            String fallback = "Cadastro de " + animal.getNome() + " concluído. O plano de cuidados inicial foi solicitado.";
            return FlowResponse.finalizarFluxoComAto(AtoComunicativo.confirmarSucesso(fallback,
                    FlowSupport.dados("animalNome", animal.getNome(), "planoPreventivo", "solicitado")));
        } catch (PetNetApiUnavailableException e) {
            log.warn("[CADASTRO] serviço indisponível tel={}: {}", telefone, e.getMessage());
            String fallback = "Não consegui concluir o cadastro agora. Pode tentar novamente em instantes?";
            return FlowResponse.comAto(AtoComunicativo.orientar(fallback,
                    FlowSupport.dados("motivo", "servico_indisponivel")));
        }
    }

    private Long cadastrarOuRecuperarResponsavel(String telefone, DadosCadastroPendente dados) {
        try {
            ResponsavelDto responsavel = petNetApiClient.cadastrarResponsavel(
                    new CadastrarResponsavelRequest(dados.getNomeTutor(), telefone));
            dados.setResponsavelId(responsavel.getId());
            return responsavel.getId();
        } catch (PetNetApiConflictException e) {
            return petNetApiClient.buscarResponsavelPorTelefone(telefone)
                    .map(ResponsavelDto::getId)
                    .orElseThrow(() -> new PetNetApiUnavailableException("telefone já cadastrado, mas responsável não foi localizado"));
        }
    }

    private void aplicarExtracaoAnimal(DadosCadastroPendente dados, String mensagem) {
        if (isBlank(dados.getNomeAnimal())) {
            extrairNomeAnimal(mensagem).ifPresent(dados::setNomeAnimal);
        }
        if (isBlank(dados.getEspecie())) {
            parserService.interpretarEspecie(mensagem).ifPresent(dados::setEspecie);
        }
        if (isBlank(dados.getPorte())) {
            parserService.interpretarPorte(mensagem).ifPresent(dados::setPorte);
        }
        if (isBlank(dados.getSexo())) {
            parserService.interpretarSexo(mensagem).ifPresent(dados::setSexo);
        }
        if (dados.getCastrado() == null) {
            parserService.interpretarCastrado(mensagem).ifPresent(dados::setCastrado);
        }
        if (isBlank(dados.getDataNascimento())) {
            parserService.interpretarDataNascimento(mensagem).ifPresent(dados::setDataNascimento);
        }
    }

    private Optional<CampoCadastro> proximoCampo(DadosCadastroPendente dados) {
        if (isBlank(dados.getNomeAnimal())) return Optional.of(CampoCadastro.NOME_ANIMAL);
        if (isBlank(dados.getEspecie())) return Optional.of(CampoCadastro.ESPECIE);
        if (isBlank(dados.getPorte())) return Optional.of(CampoCadastro.PORTE);
        if (isBlank(dados.getSexo())) return Optional.of(CampoCadastro.SEXO);
        if (dados.getCastrado() == null) return Optional.of(CampoCadastro.CASTRADO);
        if (isBlank(dados.getDataNascimento())) return Optional.of(CampoCadastro.DATA_NASCIMENTO);
        return Optional.empty();
    }

    private boolean cadastroCompleto(DadosCadastroPendente dados) {
        return (dados.getResponsavelId() != null || !isBlank(dados.getNomeTutor()))
                && proximoCampo(dados).isEmpty();
    }

    private String perguntaPara(CampoCadastro campo, DadosCadastroPendente dados) {
        return switch (campo) {
            case NOME_ANIMAL -> "Qual é o nome do animal?";
            case ESPECIE -> "Qual é a espécie? Pode ser CACHORRO, GATO, PASSARO, COELHO, HAMSTER ou OUTRO.";
            case PORTE -> "Qual é o porte? Mini, Pequeno, Médio, Grande ou Gigante?";
            case SEXO -> "Ele é macho ou fêmea?";
            case CASTRADO -> "O animal é castrado?";
            case DATA_NASCIMENTO -> "Qual é a data de nascimento ou idade aproximada de " + FlowSupport.nomeOuAnimal(dados.getNomeAnimal()) + "?";
        };
    }

    private void inferirPorteSeSeguro(DadosCadastroPendente dados) {
        if (!isBlank(dados.getPorte()) || isBlank(dados.getEspecie())) return;
        if ("GATO".equals(dados.getEspecie()) || "COELHO".equals(dados.getEspecie())) {
            dados.setPorte("PEQUENO");
        } else if ("HAMSTER".equals(dados.getEspecie()) || "PASSARO".equals(dados.getEspecie())) {
            dados.setPorte("MINI");
        }
    }

    private Optional<String> extrairNomeAnimal(String mensagem) {
        Matcher explicito = NOME_ANIMAL_EXPLICITO.matcher(mensagem == null ? "" : mensagem);
        if (explicito.find()) {
            return Optional.of(limparNome(explicito.group(1)));
        }
        Matcher aposEspecie = NOME_ANIMAL_APOS_ESPECIE.matcher(mensagem == null ? "" : mensagem);
        if (aposEspecie.find()) {
            return Optional.of(limparNome(aposEspecie.group(1)));
        }
        return Optional.empty();
    }

    private Optional<String> extrairNomeTutorExplicito(String mensagem) {
        String texto = mensagem == null ? "" : mensagem.trim();
        Matcher matcher = Pattern.compile("(?i)(?:meu nome é|meu nome e|me chamo|sou)\\s+([\\p{L} ]{3,80})").matcher(texto);
        if (matcher.find()) {
            return Optional.of(limparNome(matcher.group(1)));
        }
        return Optional.empty();
    }

    private boolean pareceDadoDeAnimal(String mensagem) {
        return parserService.interpretarEspecie(mensagem).isPresent()
                || parserService.interpretarPorte(mensagem).isPresent()
                || parserService.interpretarSexo(mensagem).isPresent()
                || parserService.interpretarCastrado(mensagem).isPresent()
                || parserService.interpretarDataNascimento(mensagem).isPresent();
    }

    private CadastroStage stageAtual(SessaoBotEntity sessao, DadosCadastroPendente dados) {
        if (sessao.getStageAtual() != null) {
            try {
                return CadastroStage.valueOf(sessao.getStageAtual());
            } catch (IllegalArgumentException ignored) {
                // Sessões antigas em desenvolvimento podem ser descartadas.
            }
        }
        if (dados.getResponsavelId() == null && isBlank(dados.getNomeTutor())) {
            return CadastroStage.CADASTRO_AGUARDANDO_NOME_TUTOR;
        }
        return CadastroStage.CADASTRO_COLETANDO_ANIMAL;
    }

    private void salvarSessaoCadastro(String telefone, CadastroStage stage, DadosCadastroPendente dados) {
        SessaoBotEntity sessao = sessaoBotRepository.findById(telefone).orElseGet(SessaoBotEntity::new);
        sessao.setTelefone(telefone);
        sessao.setFluxoAtivo(Intencao.CADASTRO);
        sessao.setStageAtual(stage.name());
        sessao.setAcaoPendente(null);
        sessao.setDadosColetados(null);
        sessao.setDadosPendentesJson(sessaoHelper.toJson(dados));
        sessao.setUpdatedAt(LocalDateTime.now(ZONE_ID));
        sessaoBotRepository.save(sessao);
    }

    private DadosCadastroPendente lerDadosCadastro(SessaoBotEntity sessao) {
        DadosCadastroPendente resultado = sessaoHelper.lerDados(sessao.getDadosPendentesJson(), DadosCadastroPendente.class);
        if (resultado != null) return resultado;
        return lerPonteAgendamento(sessao).orElseGet(DadosCadastroPendente::new);
    }

    private Optional<DadosCadastroPendente> lerPonteAgendamento(SessaoBotEntity sessao) {
        DadosAgendamentoPendente agendamento = sessaoHelper.lerDados(sessao.getDadosPendentesJson(), DadosAgendamentoPendente.class);
        if (agendamento == null) {
            log.warn("[CADASTRO] dadosPendentesJson inválido tel={}", sessao.getTelefone());
            return Optional.empty();
        }
        DadosCadastroPendente dados = new DadosCadastroPendente();
        dados.setResponsavelId(agendamento.getResponsavelId());
        dados.setFluxoRetorno(agendamento.getFluxoRetorno());
        return Optional.of(dados);
    }

    private Optional<Intencao> fluxoRetornoAtual(String telefone) {
        return sessaoBotRepository.findById(telefone)
                .map(this::lerDadosCadastro)
                .map(DadosCadastroPendente::getFluxoRetorno);
    }

    private void prepararRetornoAgendamento(String telefone) {
        DadosAgendamentoPendente dados = new DadosAgendamentoPendente();
        dados.setFluxoRetorno(Intencao.AGENDAMENTO);
        sessaoBotRepository.findById(telefone).ifPresent(sessao -> {
            sessao.setDadosPendentesJson(sessaoHelper.toJson(dados));
            sessao.setUpdatedAt(LocalDateTime.now(ZONE_ID));
            sessaoBotRepository.save(sessao);
        });
    }

    private String resumoDados(DadosCadastroPendente dados) {
        return "Tutor: " + valorOuPendente(dados.getNomeTutor()) + "\n"
                + "Animal: " + valorOuPendente(dados.getNomeAnimal()) + "\n"
                + "Espécie: " + valorOuPendente(dados.getEspecie()) + "\n"
                + "Porte: " + valorOuPendente(dados.getPorte()) + "\n"
                + "Sexo: " + valorOuPendente(dados.getSexo()) + "\n"
                + "Castrado: " + (dados.getCastrado() == null ? "pendente" : (dados.getCastrado() ? "sim" : "não")) + "\n"
                + "Nascimento: " + valorOuPendente(dados.getDataNascimento());
    }

    private Map<String, Object> dadosResumo(DadosCadastroPendente dados) {
        return FlowSupport.dados(
                "nomeTutor", dados.getNomeTutor(),
                "responsavelId", dados.getResponsavelId(),
                "nomeAnimal", dados.getNomeAnimal(),
                "especie", dados.getEspecie(),
                "porte", dados.getPorte(),
                "sexo", dados.getSexo(),
                "castrado", dados.getCastrado(),
                "dataNascimento", dados.getDataNascimento(),
                "resumo", resumoDados(dados));
    }

    private static String limparNome(String valor) {
        if (valor == null) return "";
        String limpo = valor.replaceAll("[^\\p{L} '-]", " ").replaceAll("\\s+", " ").trim();
        if (limpo.isBlank()) return "";
        String[] partes = limpo.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String parte : partes) {
            if (parte.isBlank()) continue;
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(parte.substring(0, 1).toUpperCase()).append(parte.substring(1).toLowerCase());
        }
        return sb.toString();
    }

    private static String valorOuPendente(String valor) {
        return isBlank(valor) ? "pendente" : valor;
    }

    private static boolean isBlank(String valor) {
        return valor == null || valor.isBlank();
    }
}
