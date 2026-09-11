package br.com.fiap.petbuddies.dto.checkin;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

// Devolvido ao tutor antes de qualquer calculo — nao decide nada.
// degradado=true: modelo falhou ou devolveu algo inaproveitavel, condicoes vem vazia — app cai para as perguntas fixas, nunca finge que nada foi observado.
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CheckinExtracaoResponse {

    private Long animalId;
    private LocalDate dataReferencia;
    private String narrativa;
    private List<CondicaoExtraidaResponse> condicoes;
    // Texto livre para a tela de confirmacao — nunca alimenta AvaliadorRegraService nem decide escalacao; quem julga gravidade e o catalogo, via FL_CRITICA.
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
