package br.com.fiap.petbuddies.dto;

import lombok.*;

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
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CheckinExtracaoResponse {

    private Long animalId;
    private LocalDate dataReferencia;
    private String narrativa;
    private List<CondicaoExtraidaResponse> condicoes;
    /**
     * Menções clinicamente relevantes fora do vocabulário oferecido (ex.:
     * "mancando da pata"), como texto livre — nunca um código inventado do
     * catálogo. Informativo para a tela de confirmação; não alimenta
     * {@code AvaliadorRegraService} nem decide escalação (guardrail 6:
     * quem julga gravidade é o catálogo, via FL_CRITICA, nunca texto livre
     * da IA).
     */
    private List<String> redFlags;
    private boolean degradado;

    public static CheckinExtracaoResponse of(
            Long animalId, LocalDate dataReferencia, String narrativa,
            List<CondicaoExtraidaResponse> condicoes, List<String> redFlags, boolean degradado) {
        CheckinExtracaoResponse dto = new CheckinExtracaoResponse();
        dto.animalId = animalId;
        dto.dataReferencia = dataReferencia;
        dto.narrativa = narrativa;
        dto.condicoes = condicoes;
        dto.redFlags = redFlags;
        dto.degradado = degradado;
        return dto;
    }
}
