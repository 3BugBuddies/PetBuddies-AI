package br.com.fiap.petbuddies.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * O que a IA entendeu da narrativa — para o app devolver ao tutor antes de
 * qualquer cálculo (guardrail 5: "não decide sem confirmação").
 *
 * <p>{@code degradado=true} é a degradação determinística do guardrail 4:
 * o modelo falhou ou devolveu algo inaproveitável, e {@code condicoes} vem
 * vazia — o app deve cair para as perguntas fixas, nunca fingir que nada foi
 * observado.</p>
 */
public class CheckinExtracaoResponse {

    private Long animalId;
    private LocalDate dataReferencia;
    private String narrativa;
    private List<CondicaoExtraidaResponse> condicoes;
    private boolean degradado;

    public static CheckinExtracaoResponse of(
            Long animalId, LocalDate dataReferencia, String narrativa,
            List<CondicaoExtraidaResponse> condicoes, boolean degradado) {
        CheckinExtracaoResponse dto = new CheckinExtracaoResponse();
        dto.animalId = animalId;
        dto.dataReferencia = dataReferencia;
        dto.narrativa = narrativa;
        dto.condicoes = condicoes;
        dto.degradado = degradado;
        return dto;
    }

    public Long getAnimalId() { return animalId; }
    public LocalDate getDataReferencia() { return dataReferencia; }
    public String getNarrativa() { return narrativa; }
    public List<CondicaoExtraidaResponse> getCondicoes() { return condicoes; }
    public boolean isDegradado() { return degradado; }
}
