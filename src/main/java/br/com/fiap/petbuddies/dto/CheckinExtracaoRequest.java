package br.com.fiap.petbuddies.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Passo 1 do check-in (§5.1 do documento de IA): só interpreta a narrativa e
 * devolve o que foi entendido. Não grava nada — a confirmação do tutor é o
 * passo seguinte, {@link CheckinRequest}.
 */
public class CheckinExtracaoRequest {

    @NotNull(message = "Animal é obrigatório.")
    private Long animalId;

    /** Ausente, assume hoje. */
    private LocalDate dataReferencia;

    @NotBlank(message = "Narrativa é obrigatória.")
    @Size(max = 4000, message = "Narrativa deve ter no máximo 4000 caracteres.")
    private String narrativa;

    /** Relato sobre um item específico do plano, em vez do relato geral do dia. */
    private Long itemPlanoCuidadoId;

    public Long getAnimalId() { return animalId; }
    public void setAnimalId(Long animalId) { this.animalId = animalId; }

    public LocalDate getDataReferencia() { return dataReferencia; }
    public void setDataReferencia(LocalDate dataReferencia) { this.dataReferencia = dataReferencia; }

    public String getNarrativa() { return narrativa; }
    public void setNarrativa(String narrativa) { this.narrativa = narrativa; }

    public Long getItemPlanoCuidadoId() { return itemPlanoCuidadoId; }
    public void setItemPlanoCuidadoId(Long itemPlanoCuidadoId) { this.itemPlanoCuidadoId = itemPlanoCuidadoId; }
}
