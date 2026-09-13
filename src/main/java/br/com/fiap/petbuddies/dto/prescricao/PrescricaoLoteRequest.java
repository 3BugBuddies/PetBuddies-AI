package br.com.fiap.petbuddies.dto.prescricao;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;
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
    private List<@NotNull @Valid PrescricaoRequest> prescricoes;

    @AssertTrue(message = "Todas as prescrições do lote precisam ser do mesmo registro de atendimento.")
    private boolean isMesmoAtendimento() {
        if (prescricoes == null || prescricoes.isEmpty()) {
            return true;
        }
        // Elemento nulo cai fora antes do map: o @NotNull do elemento ja responde 400 sem NPE aqui.
        return prescricoes.stream()
                .filter(Objects::nonNull)
                .map(PrescricaoRequest::getRegistroAtendimentoId)
                .distinct()
                .count() <= 1;
    }
}
