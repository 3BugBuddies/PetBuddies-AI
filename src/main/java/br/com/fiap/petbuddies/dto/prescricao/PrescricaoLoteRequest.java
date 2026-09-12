package br.com.fiap.petbuddies.dto.prescricao;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "As prescrições assinadas num mesmo atendimento. Uma prescrição é uma lista de um.")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrescricaoLoteRequest {

    // @Valid no campo, e nao so no parametro do controller: sem ele a validacao
    // para no envelope e cada PrescricaoRequest entra sem checagem nenhuma.
    @Valid
    @NotEmpty(message = "Envie ao menos uma prescrição.")
    private List<PrescricaoRequest> prescricoes;

    @AssertTrue(message = "Todas as prescrições do lote precisam ser do mesmo registro de atendimento.")
    private boolean isMesmoAtendimento() {
        if (prescricoes == null || prescricoes.isEmpty()) {
            return true;
        }
        return prescricoes.stream()
                .map(PrescricaoRequest::getRegistroAtendimentoId)
                .distinct()
                .count() == 1;
    }
}
