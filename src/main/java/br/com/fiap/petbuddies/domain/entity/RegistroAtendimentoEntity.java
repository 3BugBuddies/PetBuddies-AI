package br.com.fiap.petbuddies.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "T_PB_REGISTRO_ATENDIMENTO")
public class RegistroAtendimentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_REGISTRO_ATENDIMENTO")
    private Long id;

    @Column(name = "DT_DATA_ATENDIMENTO", nullable = false)
    private LocalDateTime dataAtendimento;

    @Column(name = "AN_ANAMNESE", length = 2000)
    private String anamnese;

    @Column(name = "DG_DIAGNOSTICO", length = 2000)
    private String diagnostico;

    @Column(name = "TR_TRATAMENTO", length = 2000)
    private String tratamento;

    @Column(name = "OB_OBSERVACAO", length = 2000)
    private String observacao;

    @Column(name = "PR_PROXIMO_RETORNO")
    private LocalDate proximoRetorno;

    @Column(name = "PR_PROXIMA_VACINA")
    private LocalDate proximaVacina;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_ANIMAL", nullable = false)
    private AnimalEntity animal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_CONSULTA", nullable = false)
    private ConsultaEntity consulta;

    @Column(name = "CA_CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "AT_UPDATED_AT")
    private LocalDateTime updatedAt;

    @PrePersist
    private void prePersist() { createdAt = LocalDateTime.now(); }

    @PreUpdate
    private void preUpdate() { updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public AnimalEntity getAnimal() { return animal; }
    public void setAnimal(AnimalEntity animal) { this.animal = animal; }

    public ConsultaEntity getConsulta() { return consulta; }
    public void setConsulta(ConsultaEntity consulta) { this.consulta = consulta; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
