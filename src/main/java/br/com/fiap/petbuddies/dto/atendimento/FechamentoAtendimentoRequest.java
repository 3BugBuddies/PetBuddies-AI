package br.com.fiap.petbuddies.dto.atendimento;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import br.com.fiap.petbuddies.dto.prescricao.PrescricaoFechamentoRequest;
import lombok.*;

import java.util.List;

/**
 * O corpo do fechamento de atendimento: registro, procedimentos executados
 * e prescrições assinadas, num único ato. Sem animalId nem
 * veterinarioId — os dois vêm da consulta no path ({@code consulta.getAnimal()}
 * e {@code consulta.getVeterinario()}), para não abrir uma segunda fonte de
 * verdade sobre quem atendeu.
 *
 * <p>{@code @Valid} nos aninhados e nos elementos das duas listas: sem isso a
 * validação para no primeiro nível e uma dose fora da faixa numa prescrição
 * no meio da lista passaria batido.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FechamentoAtendimentoRequest {

    @NotNull(message = "Registro do atendimento é obrigatório.")
    @Valid
    private RegistroAtendimentoFechamentoRequest registroAtendimento;

    @Valid
    private List<ProcedimentoFechamentoRequest> procedimentos;

    @Valid
    private List<PrescricaoFechamentoRequest> prescricoes;
}
