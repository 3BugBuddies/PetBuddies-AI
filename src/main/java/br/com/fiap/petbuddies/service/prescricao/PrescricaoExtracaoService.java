package br.com.fiap.petbuddies.service.prescricao;

import br.com.fiap.petbuddies.domain.entity.CondicaoClinicaEntity;
import br.com.fiap.petbuddies.domain.entity.RegistroAtendimentoEntity;
import br.com.fiap.petbuddies.domain.entity.VeterinarioEntity;
import br.com.fiap.petbuddies.domain.enums.prescricao.OperadorRegra;
import br.com.fiap.petbuddies.domain.enums.prescricao.TipoAcaoRegra;
import br.com.fiap.petbuddies.domain.enums.prescricao.TipoDado;
import br.com.fiap.petbuddies.domain.repository.AnimalRepository;
import br.com.fiap.petbuddies.domain.repository.CondicaoClinicaRepository;
import br.com.fiap.petbuddies.domain.repository.RegistroAtendimentoRepository;
import br.com.fiap.petbuddies.domain.repository.UsuarioRepository;
import br.com.fiap.petbuddies.domain.repository.VeterinarioRepository;
import br.com.fiap.petbuddies.dto.prescricao.PrescricaoExtracaoIA;
import br.com.fiap.petbuddies.dto.prescricao.NarrativaPrescricaoRequest;
import br.com.fiap.petbuddies.dto.prescricao.PrescricaoRequest;
import br.com.fiap.petbuddies.dto.prescricao.RascunhoPrescricaoResponse;
import br.com.fiap.petbuddies.dto.prescricao.RegraCondicionalExtraidaIA;
import br.com.fiap.petbuddies.dto.prescricao.RegraPropostaResponse;
import br.com.fiap.petbuddies.exception.atendimento.RegistroAtendimentoNaoEncontradoException;
import br.com.fiap.petbuddies.exception.atendimento.RegistroDeOutroAnimalException;
import br.com.fiap.petbuddies.exception.cadastro.AnimalNaoEncontradoException;
import br.com.fiap.petbuddies.exception.cadastro.VeterinarioNaoEncontradoException;
import br.com.fiap.petbuddies.exception.identidade.CredenciaisInvalidasException;
import br.com.fiap.petbuddies.exception.identidade.UsuarioNaoVeterinarioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import br.com.fiap.petbuddies.infrastructure.ia.ExtratorIA;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

// Nunca decide clinicamente — so preenche o formulario que o vet revisa.
// Sem @Transactional de proposito: contexto vem de JOIN FETCH atomico; a chamada ao modelo roda depois, sem transacao aberta.
@Service
public class PrescricaoExtracaoService {

    private static final Logger log = LoggerFactory.getLogger(PrescricaoExtracaoService.class);

    /** Confiança de um valor que o veterinário disse literalmente. */
    private static final double CONFIANCA_LITERAL = 1.0;

    /** Confiança de um valor que o modelo teve que inferir ou calcular (ex.: resolver "amanhã"). */
    private static final double CONFIANCA_INFERIDO = 0.6;

    private final ExtratorIA extratorIA;
    private final RegistroAtendimentoRepository registroAtendimentoRepository;
    private final CondicaoClinicaRepository condicaoClinicaRepository;
    private final AnimalRepository animalRepository;
    private final UsuarioRepository usuarioRepository;
    private final VeterinarioRepository veterinarioRepository;

    public PrescricaoExtracaoService(
            ExtratorIA extratorIA,
            RegistroAtendimentoRepository registroAtendimentoRepository,
            CondicaoClinicaRepository condicaoClinicaRepository,
            AnimalRepository animalRepository,
            UsuarioRepository usuarioRepository,
            VeterinarioRepository veterinarioRepository) {
        this.extratorIA = extratorIA;
        this.registroAtendimentoRepository = registroAtendimentoRepository;
        this.condicaoClinicaRepository = condicaoClinicaRepository;
        this.animalRepository = animalRepository;
        this.usuarioRepository = usuarioRepository;
        this.veterinarioRepository = veterinarioRepository;
    }

    public RascunhoPrescricaoResponse extrair(NarrativaPrescricaoRequest request, Long usuarioId) {
        Long animalId = request.getAnimalId();
        if (!animalRepository.existsById(animalId)) {
            throw new AnimalNaoEncontradoException(animalId);
        }
        if (request.getRegistroAtendimentoId() != null) {
            validarRegistro(request.getRegistroAtendimentoId(), animalId);
        }
        // Clínica e vet vêm do usuário logado, não do registro: o rascunho pode nascer antes de existir atendimento gravado.
        VeterinarioEntity veterinario = veterinarioDoUsuario(usuarioId);
        Long veterinarioId = veterinario.getId();
        Long clinicaId = veterinario.getClinica().getId();

        List<CondicaoClinicaEntity> catalogo = condicaoClinicaRepository.findByClinicaId(clinicaId).stream()
                .filter(CondicaoClinicaEntity::isAtivo)
                .toList();

        PrescricaoExtracaoIA extraido = chamarModelo(request.getNarrativa(), catalogo);

        if (extraido == null) {
            PrescricaoRequest vazia = prescricaoBase(animalId, veterinarioId, request.getRegistroAtendimentoId());
            return RascunhoPrescricaoResponse.degradado(
                    request.getNarrativa(),
                    "Modelo de IA indisponível ou devolveu uma resposta inválida. "
                            + "Preencha a prescrição manualmente — nenhum campo foi extraído.",
                    vazia);
        }

        PrescricaoRequest prescricao = prescricaoBase(animalId, veterinarioId, request.getRegistroAtendimentoId());
        Map<String, Double> confiancas = new LinkedHashMap<>();

        aplicarMedicamento(extraido, prescricao, confiancas);
        aplicarDoses(extraido, prescricao, confiancas);
        aplicarUnidade(extraido, prescricao, confiancas);
        aplicarFrequencia(extraido, prescricao, confiancas);
        aplicarDuracao(extraido, prescricao, confiancas);
        aplicarDataInicio(extraido, prescricao, confiancas);
        aplicarOrientacao(extraido, prescricao, confiancas);

        List<String> descartadas = new ArrayList<>();
        List<RegraPropostaResponse> regras = construirRegras(extraido.regras(), catalogo, descartadas);

        return RascunhoPrescricaoResponse.de(request.getNarrativa(), prescricao, confiancas, regras, descartadas);
    }

    private void validarRegistro(Long registroId, Long animalId) {
        RegistroAtendimentoEntity registro = registroAtendimentoRepository.findById(registroId)
                .orElseThrow(() -> new RegistroAtendimentoNaoEncontradoException(registroId));
        if (!registro.getAnimal().getId().equals(animalId)) {
            throw new RegistroDeOutroAnimalException(registroId, animalId);
        }
    }

    private VeterinarioEntity veterinarioDoUsuario(Long usuarioId) {
        Long veterinarioId = usuarioRepository.findById(usuarioId)
                .orElseThrow(CredenciaisInvalidasException::new)
                .getVeterinarioId();
        if (veterinarioId == null) {
            throw new UsuarioNaoVeterinarioException();
        }
        return veterinarioRepository.findComClinicaById(veterinarioId)
                .orElseThrow(() -> new VeterinarioNaoEncontradoException(veterinarioId));
    }

    private PrescricaoRequest prescricaoBase(Long animalId, Long veterinarioId, Long registroAtendimentoId) {
        PrescricaoRequest prescricao = new PrescricaoRequest();
        prescricao.setAnimalId(animalId);
        prescricao.setVeterinarioId(veterinarioId);
        prescricao.setRegistroAtendimentoId(registroAtendimentoId);
        return prescricao;
    }

    private PrescricaoExtracaoIA chamarModelo(String narrativa, List<CondicaoClinicaEntity> catalogo) {
        return extratorIA
                .extrair(montarPrompt(catalogo), narrativa, PrescricaoExtracaoIA.class, "PRESCRICAO-IA")
                .orElse(null);
    }

    private String montarPrompt(List<CondicaoClinicaEntity> catalogo) {
        String condicoesTexto = catalogo.isEmpty()
                ? "Nenhuma condição está cadastrada nesta clínica. Toda regra condicional deve usar \"condicao\" null e descrever a condição em \"condicaoNovaRotulo\"."
                : catalogo.stream()
                        .map(c -> "- código \"%s\" (%s): %s%s".formatted(
                                c.getCodigo(),
                                c.getTipoDado(),
                                c.getRotulo(),
                                c.getUnidade() != null && !c.getUnidade().isBlank() ? " [unidade: " + c.getUnidade() + "]" : ""))
                        .collect(Collectors.joining("\n"));

        return """
                Você transcreve a narrativa falada de um veterinário para dentro de um formulário de
                prescrição estruturado. Você NUNCA decide clinicamente, NUNCA prescreve e NUNCA inventa
                um valor que o veterinário não disse — sua única função é preencher o schema.

                Regras obrigatórias, sem exceção:

                1. Campo não mencionado na narrativa (ou mencionado de forma ambígua) volta com o valor
                   E o trecho nulos — mesmo que o schema pareça pedir um valor. Isto vale sobretudo para
                   dose, frequência e duração: se o veterinário não disse o número, o campo é null. Nunca
                   estime, nunca calcule por peso, nunca chute.
                2. Para cada campo preenchido, "trecho" é a citação (quase) literal da narrativa que
                   sustenta aquele valor — é o que a tela destaca para o vet conferir. "literal" é true
                   quando o veterinário disse o valor diretamente, e false quando você teve que inferir
                   ou calcular (ex.: resolver "amanhã" numa data, converter "meio a um" em 0.5 e 1.0,
                   normalizar a unidade). Nunca preencha um valor sem preencher o trecho correspondente.
                3. Se o veterinário citou uma dose única (ex.: "0,5 ml"), preencha doseMin e doseMax com
                   o mesmo valor. Se citou uma faixa (ex.: "1 a 2 ml"), doseMin é o menor e doseMax o
                   maior.
                4. Cada regra condicional em "regras" referencia, no campo "condicao", EXATAMENTE um dos
                   códigos listados abaixo — copie o código literalmente. Se a narrativa mencionar uma
                   condição que não está na lista (ex.: um sintoma sem código correspondente), gere a regra
                   com "condicao" null e descreva a condição nova em "condicaoNovaRotulo" (nome curto, como
                   o veterinário diria) e "condicaoNovaTipoDado" (BOOLEANO ou NUMERICO). Nunca preencha
                   "condicao" e "condicaoNovaRotulo" na mesma regra.
                5. Se a condição da regra é do tipo BOOLEANO, os campos "operador" e "limite" da
                   regra devem vir null — não há o que comparar. Se é NUMERICO, os dois são obrigatórios.
                6. "operador" só pode ser um destes literais, exatamente: MAIOR_QUE, MAIOR_OU_IGUAL,
                   MENOR_QUE, MENOR_OU_IGUAL, IGUAL.
                7. "acaoDose" só pode ser um destes literais, exatamente: DOSE_MIN, DOSE_MAX,
                   DOSE_PADRAO, ACIONAR_CLINICA, SUSPENDER.
                8. "dataInicio" é uma data no formato ISO (yyyy-MM-dd). Hoje é %s — resolva expressões
                   relativas ("a partir de amanhã", "começando segunda") contra essa data, com
                   literalDataInicio=false. Se a narrativa não mencionar quando começar, devolva null.

                Condições clínicas cadastradas nesta clínica — o vocabulário fechado que você pode usar:
                %s
                """.formatted(LocalDate.now(), condicoesTexto);
    }

    // Confianca nunca vem de auto-avaliacao do modelo — e derivada de "trecho" presente + "literal", os dois fatos verificaveis que o modelo reporta.

    private void aplicarMedicamento(PrescricaoExtracaoIA ia, PrescricaoRequest prescricao, Map<String, Double> confiancas) {
        String valor = ia.medicamento();
        if (valor == null || valor.isBlank() || valor.length() > 150 || semTrecho(ia.trechoMedicamento())) {
            return;
        }
        prescricao.setMedicamento(valor.trim());
        confiancas.put("medicamento", confiancaDe(ia.literalMedicamento()));
    }

    private void aplicarDoses(PrescricaoExtracaoIA ia, PrescricaoRequest prescricao, Map<String, Double> confiancas) {
        BigDecimal min = semTrecho(ia.trechoDoseMin()) ? null : doseValida(ia.doseMin());
        BigDecimal max = semTrecho(ia.trechoDoseMax()) ? null : doseValida(ia.doseMax());
        // CK_PRESCRICAO_FAIXA: se as duas vieram e a faixa é impossível, o par
        // inteiro é descartado — não há como adivinhar qual delas está certa.
        if (min != null && max != null && min.compareTo(max) > 0) {
            return;
        }
        if (min != null) {
            prescricao.setDoseMin(min);
            confiancas.put("doseMin", confiancaDe(ia.literalDoseMin()));
        }
        if (max != null) {
            prescricao.setDoseMax(max);
            confiancas.put("doseMax", confiancaDe(ia.literalDoseMax()));
        }
    }

    private BigDecimal decimalQueCabe(BigDecimal valor, int digitosInteiros) {
        if (valor == null) {
            return null;
        }
        BigDecimal arredondado = valor.setScale(3, RoundingMode.HALF_UP);
        if (arredondado.precision() - arredondado.scale() > digitosInteiros) {
            return null;
        }
        return arredondado;
    }

    // NR_DOSE_MIN/NR_DOSE_MAX são NUMBER(8,3): no máximo 5 dígitos inteiros e 3 decimais.
    private BigDecimal doseValida(BigDecimal valor) {
        return valor != null && valor.signum() < 0 ? null : decimalQueCabe(valor, 5);
    }

    private void aplicarUnidade(PrescricaoExtracaoIA ia, PrescricaoRequest prescricao, Map<String, Double> confiancas) {
        String valor = ia.unidade();
        if (valor == null || valor.isBlank() || valor.length() > 20 || semTrecho(ia.trechoUnidade())) {
            return;
        }
        prescricao.setUnidade(valor.trim());
        confiancas.put("unidade", confiancaDe(ia.literalUnidade()));
    }

    private void aplicarFrequencia(PrescricaoExtracaoIA ia, PrescricaoRequest prescricao, Map<String, Double> confiancas) {
        Integer valor = ia.frequenciaDia();
        if (valor == null || valor <= 0 || valor > 99 || semTrecho(ia.trechoFrequenciaDia())) {
            return;
        }
        prescricao.setFrequenciaDia(valor);
        confiancas.put("frequenciaDia", confiancaDe(ia.literalFrequenciaDia()));
    }

    private void aplicarDuracao(PrescricaoExtracaoIA ia, PrescricaoRequest prescricao, Map<String, Double> confiancas) {
        Integer valor = ia.duracaoDias();
        if (valor == null || valor <= 0 || valor > 9999 || semTrecho(ia.trechoDuracaoDias())) {
            return;
        }
        prescricao.setDuracaoDias(valor);
        confiancas.put("duracaoDias", confiancaDe(ia.literalDuracaoDias()));
    }

    private void aplicarDataInicio(PrescricaoExtracaoIA ia, PrescricaoRequest prescricao, Map<String, Double> confiancas) {
        String valor = ia.dataInicio();
        if (valor == null || valor.isBlank() || semTrecho(ia.trechoDataInicio())) {
            return;
        }
        try {
            prescricao.setDataInicio(LocalDate.parse(valor.trim()));
            confiancas.put("dataInicio", confiancaDe(ia.literalDataInicio()));
        } catch (DateTimeParseException e) {
            log.debug("[EXTRACAO_PRESCRICAO] dataInicio inválida devolvida pelo modelo: {}", valor);
        }
    }

    private void aplicarOrientacao(PrescricaoExtracaoIA ia, PrescricaoRequest prescricao, Map<String, Double> confiancas) {
        String valor = ia.orientacao();
        if (valor == null || valor.isBlank() || semTrecho(ia.trechoOrientacao())) {
            return;
        }
        prescricao.setOrientacao(valor.trim());
        confiancas.put("orientacao", confiancaDe(ia.literalOrientacao()));
    }

    private boolean semTrecho(String trecho) {
        return trecho == null || trecho.isBlank();
    }

    private double confiancaDe(Boolean literal) {
        return Boolean.TRUE.equals(literal) ? CONFIANCA_LITERAL : CONFIANCA_INFERIDO;
    }

    // --- regras condicionais propostas: cada uma amarrada a uma condição que existe no catálogo ---

    private List<RegraPropostaResponse> construirRegras(
            List<RegraCondicionalExtraidaIA> extraidas, List<CondicaoClinicaEntity> catalogo, List<String> descartadas) {
        List<RegraPropostaResponse> resultado = new ArrayList<>();
        int ordem = 1;
        for (RegraCondicionalExtraidaIA r : Objects.requireNonNullElse(extraidas, List.<RegraCondicionalExtraidaIA>of())) {
            RegraPropostaResponse regra = validarRegra(r, catalogo, ordem, descartadas);
            if (regra != null) {
                resultado.add(regra);
                ordem++;
            }
        }
        return resultado;
    }

    private RegraPropostaResponse validarRegra(
            RegraCondicionalExtraidaIA r, List<CondicaoClinicaEntity> catalogo, int ordem, List<String> descartadas) {
        boolean temCodigo = r.condicao() != null && !r.condicao().isBlank();
        boolean temNova = r.condicaoNovaRotulo() != null && !r.condicaoNovaRotulo().isBlank();
        if (!temCodigo && !temNova) {
            descartadas.add("O modelo propôs uma regra sem condição associada" + trechoEntreParenteses(r) + "; descartada.");
            return null;
        }

        CondicaoClinicaEntity condicao = temCodigo ? buscarNoCatalogo(catalogo, r.condicao()) : null;
        if (condicao == null && temNova) {
            // O modelo pode chamar de "nova" uma condição que já existe com outro código; o rótulo normalizado desempata.
            condicao = buscarPorRotulo(catalogo, r.condicaoNovaRotulo());
        }

        if (condicao == null && !temNova) {
            descartadas.add("O modelo reconheceu \"" + r.condicao()
                    + "\" na narrativa" + trechoEntreParenteses(r)
                    + ", mas esse código não está no catálogo desta clínica e nenhuma condição nova foi descrita; regra descartada.");
            return null;
        }

        TipoDado tipoDado;
        if (condicao != null) {
            tipoDado = condicao.getTipoDado();
        } else {
            String rotulo = r.condicaoNovaRotulo().trim();
            tipoDado = parseEnum(TipoDado.class, r.condicaoNovaTipoDado());
            if (tipoDado == null || rotulo.length() > 255) {
                descartadas.add("O modelo reconheceu \"" + rotulo + "\" na narrativa" + trechoEntreParenteses(r)
                        + ", mas não informou um tipo de dado válido (BOOLEANO ou NUMERICO) para criá-la; regra descartada.");
                return null;
            }
        }
        String rotuloCondicao = condicao != null ? condicao.getRotulo() : r.condicaoNovaRotulo().trim();

        TipoAcaoRegra acao = parseEnum(TipoAcaoRegra.class, r.acaoDose());
        if (acao == null) {
            descartadas.add("Ação de dose ausente ou inválida (\"" + r.acaoDose()
                    + "\") para a condição \"" + rotuloCondicao + "\"; regra descartada.");
            return null;
        }

        OperadorRegra operador = null;
        BigDecimal limite = null;
        if (tipoDado == TipoDado.NUMERICO) {
            operador = parseEnum(OperadorRegra.class, r.operador());
            limite = limiteValido(r.limite());
            if (operador == null || limite == null) {
                descartadas.add("Condição \"" + rotuloCondicao
                        + "\" é numérica e exige operador e limite válidos; a narrativa não trouxe os dois, regra descartada.");
                return null;
            }
        }
        // CK_REGRA_COERENCIA: condição BOOLEANO não aceita operador nem limite —
        // operador e limite ficam null, e a ação continua válida sozinha.

        RegraPropostaResponse.CondicaoNova nova = condicao == null
                ? new RegraPropostaResponse.CondicaoNova(rotuloCondicao, tipoDado)
                : null;
        return new RegraPropostaResponse(
                condicao == null ? null : condicao.getId(), rotuloCondicao, nova, operador, limite, acao, ordem);
    }

    private CondicaoClinicaEntity buscarNoCatalogo(List<CondicaoClinicaEntity> catalogo, String codigo) {
        return catalogo.stream()
                .filter(c -> c.getCodigo().equalsIgnoreCase(codigo.trim()))
                .findFirst()
                .orElse(null);
    }

    private CondicaoClinicaEntity buscarPorRotulo(List<CondicaoClinicaEntity> catalogo, String rotulo) {
        String alvo = semAcentoNemCaixa(rotulo);
        return catalogo.stream()
                .filter(c -> semAcentoNemCaixa(c.getRotulo()).equals(alvo))
                .findFirst()
                .orElse(null);
    }

    private String semAcentoNemCaixa(String texto) {
        return Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
    }

    private String trechoEntreParenteses(RegraCondicionalExtraidaIA r) {
        return r.trecho() == null || r.trecho().isBlank() ? "" : " (\"" + r.trecho() + "\")";
    }

    // NR_LIMITE é NUMBER(10,3): no máximo 7 dígitos inteiros e 3 decimais.
    private BigDecimal limiteValido(BigDecimal valor) {
        return decimalQueCabe(valor, 7);
    }

    private <E extends Enum<E>> E parseEnum(Class<E> tipo, String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf(tipo, valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
