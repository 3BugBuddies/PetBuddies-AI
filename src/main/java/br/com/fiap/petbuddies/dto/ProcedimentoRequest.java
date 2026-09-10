package br.com.fiap.petbuddies.dto;

import br.com.fiap.petbuddies.domain.enums.StatusProcedimento;
import br.com.fiap.petbuddies.domain.enums.TipoProcedimento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcedimentoRequest {

    @NotNull(message = "Tipo do procedimento é obrigatório.")
    private TipoProcedimento tipo;

    @NotBlank(message = "Nome é obrigatório.")
    @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres.")
    private String nome;

    @Size(max = 2000, message = "Descrição deve ter no máximo 2000 caracteres.")
    private String descricao;

    /** Ausente na criação, o procedimento nasce PENDENTE. */
    private StatusProcedimento status;

    @NotNull(message = "Data prevista de início é obrigatória.")
    private LocalDateTime dataPrevistaInicio;

    @NotNull(message = "Data prevista de fim é obrigatória.")
    private LocalDateTime dataPrevistaFim;

    @Size(max = 500, message = "URL dos anexos deve ter no máximo 500 caracteres.")
    private String anexosUrl;

    @Size(max = 2000, message = "Observação deve ter no máximo 2000 caracteres.")
    private String observacao;

    @NotNull(message = "Registro de atendimento é obrigatório.")
    private Long registroAtendimentoId;

    @NotNull(message = "Animal é obrigatório.")
    private Long animalId;

    @NotNull(message = "Veterinário é obrigatório.")
    private Long veterinarioId;
}
