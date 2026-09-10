package br.com.fiap.petbuddies.dto.atendimento;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistroAtendimentoRequest {

    @NotNull(message = "Data do atendimento é obrigatória.")
    private LocalDateTime dataAtendimento;

    @Size(max = 2000, message = "Anamnese deve ter no máximo 2000 caracteres.")
    private String anamnese;

    @Size(max = 2000, message = "Diagnóstico deve ter no máximo 2000 caracteres.")
    private String diagnostico;

    @Size(max = 2000, message = "Tratamento deve ter no máximo 2000 caracteres.")
    private String tratamento;

    @Size(max = 2000, message = "Observação deve ter no máximo 2000 caracteres.")
    private String observacao;

    private LocalDate proximoRetorno;

    private LocalDate proximaVacina;

    @NotNull(message = "Animal é obrigatório.")
    private Long animalId;

    @NotNull(message = "Consulta é obrigatória.")
    private Long consultaId;
}
