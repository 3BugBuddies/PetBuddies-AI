package br.com.fiap.petbuddies.service.prescricao;

import br.com.fiap.petbuddies.domain.entity.CondicaoClinicaEntity;
import br.com.fiap.petbuddies.domain.entity.RegistroAtendimentoEntity;
import br.com.fiap.petbuddies.domain.enums.prescricao.OperadorRegra;
import br.com.fiap.petbuddies.domain.enums.prescricao.TipoAcaoRegra;
import br.com.fiap.petbuddies.domain.enums.prescricao.TipoDado;
import br.com.fiap.petbuddies.domain.repository.CondicaoClinicaRepository;
import br.com.fiap.petbuddies.domain.repository.RegistroAtendimentoRepository;
import br.com.fiap.petbuddies.dto.prescricao.PrescricaoExtracaoIA;
import br.com.fiap.petbuddies.dto.prescricao.NarrativaPrescricaoRequest;
import br.com.fiap.petbuddies.dto.prescricao.PrescricaoRequest;
import br.com.fiap.petbuddies.dto.prescricao.RascunhoPrescricaoResponse;
import br.com.fiap.petbuddies.dto.prescricao.RegraCondicionalExtraidaIA;
import br.com.fiap.petbuddies.dto.prescricao.RegraPrescricaoRequest;
import br.com.fiap.petbuddies.exception.atendimento.RegistroAtendimentoNaoEncontradoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import br.com.fiap.petbuddies.infrastructure.ia.ExtratorIA;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    public PrescricaoExtracaoService(
            ExtratorIA extratorIA,
            RegistroAtendimentoRepository registroAtendimentoRepository,
            CondicaoClinicaRepository condicaoClinicaRepository) {
        this.extratorIA = extratorIA;
        this.registroAtendimentoRepository = registroAtendimentoRepository;
        this.condicaoClinicaRepository = condicaoClinicaRepository;
    }

    public RascunhoPrescricaoResponse extrair(NarrativaPrescricaoRequest request) {
        RegistroAtendimentoEntity registro = registroAtendimentoRepository
                .buscarComAnimalEVeterinario(request.getRegistroAtendimentoId())
                .orElseThrow(() -> new RegistroAtendimentoNaoEncontradoException(request.getRegistroAtendimentoId()));

        Long animalId = registro.getAnimal().getId();
        Long veterinarioId = registro.getConsulta().getVeterinario().getId();
        Long clinicaId = registro.getConsulta().getVeterinario().getClinica().getId();

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
        List<RegraPrescricaoRequest> regras = construirRegras(extraido.regras(), catalogo, descartadas);

        return RascunhoPrescricaoResponse.de(request.getNarrativa(), prescricao, confiancas, regras, descartadas);
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
                ? "Nenhuma condição está cadastrada nesta clínica. NÃO proponha nenhuma regra condicional — devolva a lista \"regras\" vazia."
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
                4. Cada regra condicional em "regras" deve referenciar, no campo "condicao", EXATAMENTE
                   um dos códigos listados abaixo — copie o código literalmente. O vocabulário é fechado,
                   definido pela clínica: se a narrativa mencionar uma condição que não está na lista
                   (ex.: um sintoma sem código correspondente), NÃO gere regra nenhuma para ela.
                5. Se a condição referenciada é do tipo BOOLEANO, os campos "operador" e "limite" da
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

    // NR_DOSE_MIN/NR_DOSE_MAX são NUMBER(8,3): no máximo 5 dígitos inteiros e 3 decimais.
    private BigDecimal doseValida(BigDecimal valor) {
        if (valor == null || valor.signum() < 0) {
            return null;
        }
        BigDecimal arredondado = valor.setScale(3, RoundingMode.HALF_UP);
        if (arredondado.precision() - arredondado.scale() > 5) {
            return null;
        }
        return arredondado;
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

    private List<RegraPrescricaoRequest> construirRegras(
            List<RegraCondicionalExtraidaIA> extraidas, List<CondicaoClinicaEntity> catalogo, List<String> descartadas) {
        List<RegraPrescricaoRequest> resultado = new ArrayList<>();
        int ordem = 1;
        for (RegraCondicionalExtraidaIA r : nullSafe(extraidas)) {
            RegraPrescricaoRequest regra = validarRegra(r, catalogo, ordem, descartadas);
            if (regra != null) {
                resultado.add(regra);
                ordem++;
            }
        }
        return resultado;
    }

    private RegraPrescricaoRequest validarRegra(
            RegraCondicionalExtraidaIA r, List<CondicaoClinicaEntity> catalogo, int ordem, List<String> descartadas) {
        if (r.condicao() == null || r.condicao().isBlank()) {
            descartadas.add("O modelo propôs uma regra sem condição associada" + trechoEntreParenteses(r) + "; descartada.");
            return null;
        }
        CondicaoClinicaEntity condicao = catalogo.stream()
                .filter(c -> c.getCodigo().equalsIgnoreCase(r.condicao().trim()))
                .findFirst()
                .orElse(null);
        if (condicao == null) {
            // Condicao que o modelo apontou nao existe no catalogo desta clinica — regra descartada, nunca gravada.
            descartadas.add("O modelo reconheceu \"" + r.condicao()
                    + "\" na narrativa" + trechoEntreParenteses(r)
                    + ", mas essa condição não está cadastrada no catálogo desta clínica; regra descartada.");
            return null;
        }

        TipoAcaoRegra acao = parseEnum(TipoAcaoRegra.class, r.acaoDose());
        if (acao == null) {
            descartadas.add("Ação de dose ausente ou inválida (\"" + r.acaoDose()
                    + "\") para a condição \"" + condicao.getCodigo() + "\"; regra descartada.");
            return null;
        }

        boolean numerico = condicao.getTipoDado() == TipoDado.NUMERICO;
        OperadorRegra operador = null;
        BigDecimal limite = null;
        if (numerico) {
            operador = parseEnum(OperadorRegra.class, r.operador());
            limite = limiteValido(r.limite());
            if (operador == null || limite == null) {
                descartadas.add("Condição \"" + condicao.getCodigo()
                        + "\" é numérica e exige operador e limite válidos; a narrativa não trouxe os dois, regra descartada.");
                return null;
            }
        }
        // CK_REGRA_COERENCIA: condição BOOLEANO não aceita operador nem limite —
        // operador e limite ficam null, e a ação continua válida sozinha.

        RegraPrescricaoRequest regra = new RegraPrescricaoRequest();
        regra.setCondicaoClinicaId(condicao.getId());
        regra.setOperador(operador);
        regra.setLimite(limite);
        regra.setAcaoDose(acao);
        regra.setOrdem(ordem);
        return regra;
    }

    private String trechoEntreParenteses(RegraCondicionalExtraidaIA r) {
        return r.trecho() == null || r.trecho().isBlank() ? "" : " (\"" + r.trecho() + "\")";
    }

    // NR_LIMITE é NUMBER(10,3): no máximo 7 dígitos inteiros e 3 decimais.
    private BigDecimal limiteValido(BigDecimal valor) {
        if (valor == null) {
            return null;
        }
        BigDecimal arredondado = valor.setScale(3, RoundingMode.HALF_UP);
        if (arredondado.precision() - arredondado.scale() > 7) {
            return null;
        }
        return arredondado;
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

    private static <T> List<T> nullSafe(List<T> lista) {
        return lista == null ? List.of() : lista;
    }
}
