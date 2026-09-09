package br.com.fiap.petbuddies.dto;

import br.com.fiap.petbuddies.domain.entity.AnimalEntity;
import br.com.fiap.petbuddies.domain.enums.Especie;
import br.com.fiap.petbuddies.domain.enums.Porte;
import br.com.fiap.petbuddies.domain.enums.Sexo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AnimalResponse {

    private Long id;
    private String nome;
    private Especie especie;
    private String raca;
    private Porte porte;
    private Sexo sexo;
    private LocalDate dataNascimento;
    private BigDecimal peso;
    private Boolean condicaoCronica;
    private Boolean castrado;
    private String foto;
    private String alergias;
    private String observacoes;
    private Long responsavelId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AnimalResponse from(AnimalEntity entity) {
        AnimalResponse dto = new AnimalResponse();
        dto.id = entity.getId();
        dto.nome = entity.getNome();
        dto.especie = entity.getEspecie();
        dto.raca = entity.getRaca();
        dto.porte = entity.getPorte();
        dto.sexo = entity.getSexo();
        dto.dataNascimento = entity.getDataNascimento();
        dto.peso = entity.getPeso();
        dto.condicaoCronica = entity.isCondicaoCronica();
        dto.castrado = entity.isCastrado();
        dto.foto = entity.getFoto();
        dto.alergias = entity.getAlergias();
        dto.observacoes = entity.getObservacoes();
        // Só o id: com open-in-view=false, ler outro campo do proxy LAZY aqui lança LazyInitializationException.
        dto.responsavelId = entity.getResponsavel() == null ? null : entity.getResponsavel().getId();
        dto.createdAt = entity.getCreatedAt();
        dto.updatedAt = entity.getUpdatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public Especie getEspecie() { return especie; }
    public String getRaca() { return raca; }
    public Porte getPorte() { return porte; }
    public Sexo getSexo() { return sexo; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public BigDecimal getPeso() { return peso; }
    public Boolean getCondicaoCronica() { return condicaoCronica; }
    public Boolean getCastrado() { return castrado; }
    public String getFoto() { return foto; }
    public String getAlergias() { return alergias; }
    public String getObservacoes() { return observacoes; }
    public Long getResponsavelId() { return responsavelId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
