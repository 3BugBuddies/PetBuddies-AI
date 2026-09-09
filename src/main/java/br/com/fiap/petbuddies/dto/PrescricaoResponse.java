package br.com.fiap.petbuddies.dto;

import br.com.fiap.petbuddies.domain.entity.PrescricaoEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PrescricaoResponse {

    private Long id;
    private String medicamento;
    private BigDecimal doseMin;
    private BigDecimal doseMax;
    private String unidade;
    private Integer frequenciaDia;
    private Integer duracaoDias;
    private LocalDate dataInicio;
    private String orientacao;
    private Long materialOrigemId;
    private Integer versaoOrigem;
    private Long animalId;
    private Long veterinarioId;
    private Long registroAtendimentoId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PrescricaoResponse from(PrescricaoEntity entity) {
        PrescricaoResponse dto = new PrescricaoResponse();
        dto.id = entity.getId();
        dto.medicamento = entity.getMedicamento();
        dto.doseMin = entity.getDoseMin();
        dto.doseMax = entity.getDoseMax();
        dto.unidade = entity.getUnidade();
        dto.frequenciaDia = entity.getFrequenciaDia();
        dto.duracaoDias = entity.getDuracaoDias();
        dto.dataInicio = entity.getDataInicio();
        dto.orientacao = entity.getOrientacao();
        dto.materialOrigemId = entity.getMaterialOrigemId();
        dto.versaoOrigem = entity.getVersaoOrigem();
        dto.animalId = entity.getAnimal() == null ? null : entity.getAnimal().getId();
        dto.veterinarioId = entity.getVeterinario() == null ? null : entity.getVeterinario().getId();
        dto.registroAtendimentoId = entity.getRegistroAtendimento() == null ? null : entity.getRegistroAtendimento().getId();
        dto.createdAt = entity.getCreatedAt();
        dto.updatedAt = entity.getUpdatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public String getMedicamento() { return medicamento; }
    public BigDecimal getDoseMin() { return doseMin; }
    public BigDecimal getDoseMax() { return doseMax; }
    public String getUnidade() { return unidade; }
    public Integer getFrequenciaDia() { return frequenciaDia; }
    public Integer getDuracaoDias() { return duracaoDias; }
    public LocalDate getDataInicio() { return dataInicio; }
    public String getOrientacao() { return orientacao; }
    public Long getMaterialOrigemId() { return materialOrigemId; }
    public Integer getVersaoOrigem() { return versaoOrigem; }
    public Long getAnimalId() { return animalId; }
    public Long getVeterinarioId() { return veterinarioId; }
    public Long getRegistroAtendimentoId() { return registroAtendimentoId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
