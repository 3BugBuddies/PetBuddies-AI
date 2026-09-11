package br.com.fiap.petbuddies.service.checkin;

import br.com.fiap.petbuddies.domain.entity.AnimalEntity;
import br.com.fiap.petbuddies.domain.entity.CondicaoClinicaEntity;
import br.com.fiap.petbuddies.domain.entity.PrescricaoEntity;
import br.com.fiap.petbuddies.domain.enums.prescricao.TipoDado;
import br.com.fiap.petbuddies.domain.enums.prescricao.TipoFonteValor;
import br.com.fiap.petbuddies.domain.repository.AnimalRepository;
import br.com.fiap.petbuddies.domain.repository.CondicaoClinicaRepository;
import br.com.fiap.petbuddies.domain.repository.RegraPrescricaoRepository;
import br.com.fiap.petbuddies.dto.checkin.CheckinExtracaoRequest;
import br.com.fiap.petbuddies.dto.checkin.CheckinExtracaoResponse;
import br.com.fiap.petbuddies.dto.checkin.CondicaoExtraidaResponse;
import br.com.fiap.petbuddies.exception.cadastro.AnimalNaoEncontradoException;
import br.com.fiap.petbuddies.service.prescricao.PrescricaoAtivaResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import br.com.fiap.petbuddies.infrastructure.ia.ExtratorIA;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// So interpreta a narrativa — nunca grava, nunca decide dose. Confirmacao do tutor e o CheckinService, em passo separado.
@Service
public class CheckinExtracaoService {

    private static final Logger log = LoggerFactory.getLogger(CheckinExtracaoService.class);

    private final ExtratorIA extratorIA;
    private final AnimalRepository animalRepository;
    private final RegraPrescricaoRepository regraPrescricaoRepository;
    private final CondicaoClinicaRepository condicaoClinicaRepository;
    private final PrescricaoAtivaResolver prescricaoAtivaResolver;

    public CheckinExtracaoService(
            ExtratorIA extratorIA,
            AnimalRepository animalRepository,
            RegraPrescricaoRepository regraPrescricaoRepository,
            CondicaoClinicaRepository condicaoClinicaRepository,
            PrescricaoAtivaResolver prescricaoAtivaResolver) {
        this.extratorIA = extratorIA;
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

        Optional<ExtracaoModelo> retorno = extratorIA.extrair(
                prompt, request.getNarrativa(), ExtracaoModelo.class, "CHECKIN-IA");
        boolean degradado = retorno.isEmpty();
        ExtracaoModelo resultadoModelo = retorno.orElseGet(() -> new ExtracaoModelo(List.of(), List.of()));

        List<CondicaoExtraidaResponse> condicoes = filtrarEEnriquecer(resultadoModelo.condicoes(), vocabulario);
        List<String> redFlags = nullSafe(resultadoModelo.redFlags());
        return CheckinExtracaoResponse.of(animal.getId(), referencia, request.getNarrativa(), condicoes, redFlags, degradado);
    }

    // Uniao de condicoes por regra ativa com toda condicao critica ativa; restrita a RELATO — ANIMAL/HISTORICO nao e algo que o tutor narra.
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

    // Descarta codigo fora do vocabulario, confianca fora de [0,1], e valor incoerente com TP_DADO — nunca deixa o modelo inventar campo.
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
                // CK_COBS_UM_VALOR e garantido aqui, nao pelo prompt — NUMERICO nunca sai com valorBooleano, e vice-versa.
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

    // trecho e literal nao sao persistidos — servem para ancorar a confianca no texto e a tela de confirmacao mostrar a origem do campo.
    public record CondicaoExtraidaModelo(
            String codigo, Boolean valorBooleano, BigDecimal valorNumerico,
            String trecho, Boolean literal, Double confianca) {}
}
