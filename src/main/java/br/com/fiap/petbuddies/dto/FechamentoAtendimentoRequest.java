package br.com.fiap.petbuddies.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

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
public class FechamentoAtendimentoRequest {

    @NotNull(message = "Registro do atendimento é obrigatório.")
    @Valid
    private RegistroAtendimentoFechamentoRequest registroAtendimento;

    @Valid
    private List<ProcedimentoFechamentoRequest> procedimentos;

    @Valid
    private List<PrescricaoFechamentoRequest> prescricoes;

    public RegistroAtendimentoFechamentoRequest getRegistroAtendimento() { return registroAtendimento; }
    public void setRegistroAtendimento(RegistroAtendimentoFechamentoRequest registroAtendimento) { this.registroAtendimento = registroAtendimento; }

    public List<ProcedimentoFechamentoRequest> getProcedimentos() { return procedimentos; }
    public void setProcedimentos(List<ProcedimentoFechamentoRequest> procedimentos) { this.procedimentos = procedimentos; }

    public List<PrescricaoFechamentoRequest> getPrescricoes() { return prescricoes; }
    public void setPrescricoes(List<PrescricaoFechamentoRequest> prescricoes) { this.prescricoes = prescricoes; }
}
