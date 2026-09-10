package br.com.fiap.petbuddies.dto;

import br.com.fiap.petbuddies.domain.enums.TipoConsulta;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** A consulta nasce AGENDADA, com data/hora e veterinário herdados da janela ocupada. */
public class AgendamentoRequest {

    @NotNull(message = "Janela de atendimento é obrigatória.")
    private Long janelaId;

    @NotNull(message = "Animal é obrigatório.")
    private Long animalId;

    @NotNull(message = "Tipo da consulta é obrigatório.")
    private TipoConsulta tipo;

    @Size(max = 2000, message = "Observação deve ter no máximo 2000 caracteres.")
    private String observacao;

    public Long getJanelaId() { return janelaId; }
    public void setJanelaId(Long janelaId) { this.janelaId = janelaId; }

    public Long getAnimalId() { return animalId; }
    public void setAnimalId(Long animalId) { this.animalId = animalId; }

    public TipoConsulta getTipo() { return tipo; }
    public void setTipo(TipoConsulta tipo) { this.tipo = tipo; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}
