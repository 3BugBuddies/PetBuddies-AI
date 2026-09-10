package br.com.fiap.petbuddies.service;

import br.com.fiap.petbuddies.domain.entity.AnimalEntity;
import br.com.fiap.petbuddies.domain.entity.CondicaoClinicaEntity;
import br.com.fiap.petbuddies.domain.entity.PrescricaoEntity;
import br.com.fiap.petbuddies.domain.enums.TipoDado;
import br.com.fiap.petbuddies.domain.enums.TipoFonteValor;
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
        this.chatClient = chatClientBuilder.build();
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
            return CheckinExtracaoResponse.of(animal.getId(), referencia, request.getNarrativa(), List.of(), false);
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
                resultadoModelo = new ExtracaoModelo(List.of());
            }
        } catch (Exception e) {
            log.warn("[CHECKIN-IA] falha na extração para animalId={}: {}", animal.getId(), e.getMessage());
            degradado = true;
            resultadoModelo = new ExtracaoModelo(List.of());
        }
        log.debug("[CHECKIN-IA] resposta do modelo: {}", resultadoModelo);

        List<CondicaoExtraidaResponse> condicoes = filtrarEEnriquecer(resultadoModelo.condicoes(), vocabulario);
        return CheckinExtracaoResponse.of(animal.getId(), referencia, request.getNarrativa(), condicoes, degradado);
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
                - "confianca": sua confiança nesta extração específica, entre 0.0 e 1.0

                NÃO inclua condição que o tutor não mencionou — a ausência do item é o "não sei".
                Mencionar e negar é diferente de não mencionar: "as fezes estavam normais" gera um
                item com valorBooleano=false, nunca a ausência do item.

                Responda APENAS com JSON válido, sem explicação:
                {"condicoes":[{"codigo":"...","valorBooleano":null,"valorNumerico":null,"confianca":0.0}]}
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
                continue;
            }

            resultado.add(CondicaoExtraidaResponse.of(
                    condicao.getId(), condicao.getCodigo(), condicao.getRotulo(), condicao.getTipoDado(),
                    condicao.getUnidade(), bruta.valorBooleano(), bruta.valorNumerico(), bruta.confianca(),
                    condicao.isCritica()));
        }
        return resultado;
    }

    private static <T> List<T> nullSafe(List<T> list) {
        return list == null ? List.of() : list;
    }

    /** Contrato de structured output do Spring AI — não cruza a borda do controller. */
    public record ExtracaoModelo(List<CondicaoExtraidaModelo> condicoes) {}

    public record CondicaoExtraidaModelo(String codigo, Boolean valorBooleano, BigDecimal valorNumerico, Double confianca) {}
}
