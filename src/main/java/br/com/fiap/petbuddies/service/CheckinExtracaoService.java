package br.com.fiap.petbuddies.service;

import br.com.fiap.petbuddies.domain.entity.AnimalEntity;
import br.com.fiap.petbuddies.domain.entity.CondicaoClinicaEntity;
import br.com.fiap.petbuddies.domain.entity.PrescricaoEntity;
import br.com.fiap.petbuddies.domain.enums.prescricao.TipoDado;
import br.com.fiap.petbuddies.domain.enums.prescricao.TipoFonteValor;
import br.com.fiap.petbuddies.domain.repository.AnimalRepository;
import br.com.fiap.petbuddies.domain.repository.CondicaoClinicaRepository;
import br.com.fiap.petbuddies.domain.repository.RegraPrescricaoRepository;
import br.com.fiap.petbuddies.dto.CheckinExtracaoRequest;
import br.com.fiap.petbuddies.dto.CheckinExtracaoResponse;
import br.com.fiap.petbuddies.dto.CondicaoExtraidaResponse;
import br.com.fiap.petbuddies.exception.AnimalNaoEncontradoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Componente A do documento de IA: NLP por structured output (Spring AI +
 * Gemini, ADR s3-13). Só interpreta a narrativa e devolve o que entendeu —
 * nunca grava, nunca decide dose (guardrail 1). A confirmação do tutor é o
 * {@link CheckinService}, num passo separado.
 */
@Service
public class CheckinExtracaoService {

    private static final Logger log = LoggerFactory.getLogger(CheckinExtracaoService.class);

    private final ChatClient chatClient;
    private final AnimalRepository animalRepository;
    private final RegraPrescricaoRepository regraPrescricaoRepository;
    private final CondicaoClinicaRepository condicaoClinicaRepository;
    private final PrescricaoAtivaResolver prescricaoAtivaResolver;

    public CheckinExtracaoService(
            ChatClient.Builder chatClientBuilder,
            AnimalRepository animalRepository,
            RegraPrescricaoRepository regraPrescricaoRepository,
            CondicaoClinicaRepository condicaoClinicaRepository,
            PrescricaoAtivaResolver prescricaoAtivaResolver) {
        // temperature=0: testado contra a API real — sem isso a confianca
        // nao calibra, sai 1.0 em tudo (ver corpo do PR).
        this.chatClient = chatClientBuilder
                .defaultOptions(OpenAiChatOptions.builder().temperature(0.0).build())
                .build();
        this.animalRepository = animalRepository;
        this.regraPrescricaoRepository = regraPrescricaoRepository;
        this.condicaoClinicaRepository = condicaoClinicaRepository;
        this.prescricaoAtivaResolver = prescricaoAtivaResolver;
    }

    public CheckinExtracaoResponse extrair(CheckinExtracaoRequest request) {
        LocalDate referencia = request.getDataReferencia() == null ? LocalDate.now() : request.getDataReferencia();
        AnimalEntity animal = animalRepository.findById(request.getAnimalId())
                .orElseThrow(() -> new AnimalNaoEncontradoException(request.getAnimalId()));

        List<CondicaoClinicaEntity> vocabulario = montarVocabulario(animal.getId(), referencia);
        if (vocabulario.isEmpty()) {
            // Sem prescrição ativa com regra por relato e sem condição crítica
            // cadastrada: nada para o modelo extrair. Não chama o modelo à toa.
            return CheckinExtracaoResponse.of(animal.getId(), referencia, request.getNarrativa(), List.of(), List.of(), false);
        }

        String prompt = montarPrompt(vocabulario);
        log.debug("[CHECKIN-IA] animalId={} vocabulario={} prompt=\n{}", animal.getId(), vocabulario.size(), prompt);

        ExtracaoModelo resultadoModelo;
        boolean degradado = false;
        try {
            resultadoModelo = chatClient.prompt().system(prompt).user(request.getNarrativa())
                    .call().entity(ExtracaoModelo.class);
            if (resultadoModelo == null) {
                degradado = true;
                resultadoModelo = new ExtracaoModelo(List.of(), List.of());
            }
        } catch (Exception e) {
            log.warn("[CHECKIN-IA] falha na extração para animalId={}: {}", animal.getId(), e.getMessage());
            degradado = true;
            resultadoModelo = new ExtracaoModelo(List.of(), List.of());
        }
        log.debug("[CHECKIN-IA] resposta do modelo: {}", resultadoModelo);

        List<CondicaoExtraidaResponse> condicoes = filtrarEEnriquecer(resultadoModelo.condicoes(), vocabulario);
        List<String> redFlags = nullSafe(resultadoModelo.redFlags());
        return CheckinExtracaoResponse.of(animal.getId(), referencia, request.getNarrativa(), condicoes, redFlags, degradado);
    }

    /**
     * União de (condições referenciadas por regra de prescrição ativa) com
     * (toda condição crítica ativa, ADR s3-16 — escala independente de
     * prescrição). Restrita a {@code TP_FONTE_VALOR=RELATO}: o que vem de
     * ANIMAL ou HISTORICO não é algo que o tutor narra.
     */
    private List<CondicaoClinicaEntity> montarVocabulario(Long animalId, LocalDate referencia) {
        List<PrescricaoEntity> ativas = prescricaoAtivaResolver.listar(animalId, referencia);

        Set<Long> idsPorRegra = ativas.stream()
                .flatMap(p -> regraPrescricaoRepository.findByPrescricaoIdOrderByOrdemAsc(p.getId()).stream())
                .filter(r -> r.getFonteValorCongelada() == TipoFonteValor.RELATO)
                .map(r -> r.getCondicaoClinica().getId())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<Long, CondicaoClinicaEntity> vocabulario = new LinkedHashMap<>();
        if (!idsPorRegra.isEmpty()) {
            condicaoClinicaRepository.findAllById(idsPorRegra).forEach(c -> vocabulario.put(c.getId(), c));
        }
        condicaoClinicaRepository.findByCriticaTrueAndAtivoTrue().forEach(c -> vocabulario.putIfAbsent(c.getId(), c));

        return vocabulario.values().stream()
                .filter(CondicaoClinicaEntity::isAtivo)
                .filter(c -> c.getFonteValor() == TipoFonteValor.RELATO)
                .toList();
    }

    private String montarPrompt(List<CondicaoClinicaEntity> vocabulario) {
        String itens = vocabulario.stream()
                .map(c -> "- codigo=\"%s\" | rotulo=\"%s\" | tipo=%s%s".formatted(
                        c.getCodigo(), c.getRotulo(), c.getTipoDado(),
                        c.getUnidade() == null ? "" : " | unidade=" + c.getUnidade()))
                .collect(Collectors.joining("\n"));

        return """
                Você lê o relato de um tutor sobre o tratamento do animal dele e extrai SOMENTE as
                condições abaixo, vindas do catálogo clínico da prescrição. Nunca invente um código
                fora desta lista:

                %s

                Para cada condição que o tutor mencionar — afirmando OU negando — devolva um item com:
                - "codigo": exatamente um dos códigos acima
                - "valorBooleano": true ou false, somente para condição de tipo BOOLEANO (nulo se a condição for NUMERICO)
                - "valorNumerico": o número relatado, somente para condição de tipo NUMERICO (nulo se a condição for BOOLEANO)

                ATENÇÃO — condição NUMERICO só entra no array se a narrativa trouxer um NÚMERO medido.
                Se o tutor apenas disse que está normal, sem medir, NÃO inclua a condição NUMERICO.
                NUNCA preencha "valorBooleano" numa condição de tipo NUMERICO.

                - "trecho": as palavras EXATAS da narrativa que embasam esta extração (copie, não parafraseie)
                - "literal": true se o tutor disse diretamente; false se você inferiu a partir do contexto
                - "confianca": CALIBRADA com base em quão direto foi o relato, nunca 1.0 por padrão:
                    0.95-1.0 = o tutor deu o dado explicitamente, com as palavras da própria condição
                    0.7-0.9  = disse com outras palavras, mas o sentido é claro
                    0.4-0.6  = inferência razoável a partir do contexto, não uma afirmação direta
                    0.1-0.3  = palpite fraco
                  Se toda condição desta resposta sair com a mesma confiança, você não calibrou.

                NÃO inclua condição que o tutor não mencionou — a ausência do item é o "não sei".
                Mencionar e negar é diferente de não mencionar: "as fezes estavam normais" gera um
                item com valorBooleano=false, nunca a ausência do item.

                Se a narrativa citar algo clinicamente relevante que NÃO está no vocabulário acima
                (ex.: mancando, gemendo, sangramento em outro lugar), copie a frase para "redFlags"
                como texto livre — nunca invente um código do vocabulário para isso.

                Responda APENAS com JSON válido, sem explicação:
                {"condicoes":[{"codigo":"...","valorBooleano":null,"valorNumerico":null,"trecho":"...","literal":true,"confianca":0.0}],"redFlags":[]}
                """.formatted(itens);
    }

    /**
     * Degradação determinística (guardrail 4): descarta código fora do
     * vocabulário oferecido, confiança fora de [0,1], e valor incoerente com
     * o TP_DADO da condição — nunca deixa o modelo inventar campo.
     */
    private List<CondicaoExtraidaResponse> filtrarEEnriquecer(
            List<CondicaoExtraidaModelo> brutas, List<CondicaoClinicaEntity> vocabulario) {
        Map<String, CondicaoClinicaEntity> porCodigo = vocabulario.stream()
                .collect(Collectors.toMap(CondicaoClinicaEntity::getCodigo, c -> c));

        List<CondicaoExtraidaResponse> resultado = new ArrayList<>();
        for (CondicaoExtraidaModelo bruta : nullSafe(brutas)) {
            if (bruta == null || bruta.codigo() == null) {
                continue;
            }
            CondicaoClinicaEntity condicao = porCodigo.get(bruta.codigo());
            if (condicao == null) {
                log.warn("[CHECKIN-IA] modelo devolveu código fora do vocabulário: {}", bruta.codigo());
                continue;
            }
            if (bruta.confianca() == null || bruta.confianca() < 0.0 || bruta.confianca() > 1.0) {
                continue;
            }
            boolean numerico = condicao.getTipoDado() == TipoDado.NUMERICO;
            boolean temBooleano = bruta.valorBooleano() != null;
            boolean temNumerico = bruta.valorNumerico() != null;
            boolean valorCoerente = numerico ? (temNumerico && !temBooleano) : (temBooleano && !temNumerico);
            if (!valorCoerente) {
                // O prompt já instrui o modelo a respeitar TP_DADO, mas quem
                // garante CK_COBS_UM_VALOR é este filtro — nunca o prompt.
                // Uma condição NUMERICO nunca vira linha com valorBooleano, e
                // vice-versa, mesmo que o modelo erre a instrução.
                log.warn("[CHECKIN-IA] descartada por tipo incoerente: codigo={} tipoDado={} valorBooleano={} valorNumerico={}",
                        bruta.codigo(), condicao.getTipoDado(), bruta.valorBooleano(), bruta.valorNumerico());
                continue;
            }

            resultado.add(CondicaoExtraidaResponse.of(
                    condicao.getId(), condicao.getCodigo(), condicao.getRotulo(), condicao.getTipoDado(),
                    condicao.getUnidade(), bruta.valorBooleano(), bruta.valorNumerico(), bruta.confianca(),
                    condicao.isCritica(), bruta.trecho(), Boolean.TRUE.equals(bruta.literal())));
        }
        return resultado;
    }

    private static <T> List<T> nullSafe(List<T> list) {
        return list == null ? List.of() : list;
    }

    /** Contrato de structured output do Spring AI — não cruza a borda do controller. */
    public record ExtracaoModelo(List<CondicaoExtraidaModelo> condicoes, List<String> redFlags) {}

    /**
     * {@code trecho} e {@code literal} não são persistidos (nenhuma tabela
     * tem coluna pra eles) — servem só para ancorar a confiança no texto e
     * para a tela de confirmação do tutor mostrar de onde cada campo veio.
     * Sem eles a confiança sai 1.0 em tudo, verificado contra a API real
     * (ver corpo do PR).
     */
    public record CondicaoExtraidaModelo(
            String codigo, Boolean valorBooleano, BigDecimal valorNumerico,
            String trecho, Boolean literal, Double confianca) {}
}
