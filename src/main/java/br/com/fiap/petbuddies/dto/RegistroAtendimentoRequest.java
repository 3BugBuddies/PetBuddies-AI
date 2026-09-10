package br.com.fiap.petbuddies.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    public LocalDateTime getDataAtendimento() { return dataAtendimento; }
    public void setDataAtendimento(LocalDateTime dataAtendimento) { this.dataAtendimento = dataAtendimento; }

    public String getAnamnese() { return anamnese; }
    public void setAnamnese(String anamnese) { this.anamnese = anamnese; }

    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }

    public String getTratamento() { return tratamento; }
    public void setTratamento(String tratamento) { this.tratamento = tratamento; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }

    public LocalDate getProximoRetorno() { return proximoRetorno; }
    public void setProximoRetorno(LocalDate proximoRetorno) { this.proximoRetorno = proximoRetorno; }

    public LocalDate getProximaVacina() { return proximaVacina; }
    public void setProximaVacina(LocalDate proximaVacina) { this.proximaVacina = proximaVacina; }

    public Long getAnimalId() { return animalId; }
    public void setAnimalId(Long animalId) { this.animalId = animalId; }

    public Long getConsultaId() { return consultaId; }
    public void setConsultaId(Long consultaId) { this.consultaId = consultaId; }
}
