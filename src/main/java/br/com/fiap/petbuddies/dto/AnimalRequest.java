package br.com.fiap.petbuddies.dto;

import br.com.fiap.petbuddies.domain.enums.Especie;
import br.com.fiap.petbuddies.domain.enums.Porte;
import br.com.fiap.petbuddies.domain.enums.Sexo;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AnimalRequest {

    @NotBlank(message = "Nome é obrigatório.")
    @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres.")
    private String nome;

    @NotNull(message = "Espécie é obrigatória.")
    private Especie especie;

    @Size(max = 100, message = "Raça deve ter no máximo 100 caracteres.")
    private String raca;

    @NotNull(message = "Porte é obrigatório.")
    private Porte porte;

    @NotNull(message = "Sexo é obrigatório.")
    private Sexo sexo;

    @NotNull(message = "Data de nascimento é obrigatória.")
    @PastOrPresent(message = "Data de nascimento não pode ser futura.")
    private LocalDate dataNascimento;

    // NR_PESO é NUMBER(5,2): três dígitos inteiros e duas casas.
    @DecimalMin(value = "0.0", message = "Peso não pode ser negativo.")
    @DecimalMax(value = "999.99", message = "Peso deve ser no máximo 999,99.")
    private BigDecimal peso;

    private Boolean condicaoCronica;

    private Boolean castrado;

    @Size(max = 500, message = "Foto deve ter no máximo 500 caracteres.")
    private String foto;

    @Size(max = 2000, message = "Alergias devem ter no máximo 2000 caracteres.")
    private String alergias;

    @Size(max = 2000, message = "Observações devem ter no máximo 2000 caracteres.")
    private String observacoes;

    @NotNull(message = "Responsável é obrigatório.")
    private Long responsavelId;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Especie getEspecie() { return especie; }
    public void setEspecie(Especie especie) { this.especie = especie; }

    public String getRaca() { return raca; }
    public void setRaca(String raca) { this.raca = raca; }

    public Porte getPorte() { return porte; }
    public void setPorte(Porte porte) { this.porte = porte; }

    public Sexo getSexo() { return sexo; }
    public void setSexo(Sexo sexo) { this.sexo = sexo; }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }

    public BigDecimal getPeso() { return peso; }
    public void setPeso(BigDecimal peso) { this.peso = peso; }

    public Boolean getCondicaoCronica() { return condicaoCronica; }
    public void setCondicaoCronica(Boolean condicaoCronica) { this.condicaoCronica = condicaoCronica; }

    public Boolean getCastrado() { return castrado; }
    public void setCastrado(Boolean castrado) { this.castrado = castrado; }

    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }

    public String getAlergias() { return alergias; }
    public void setAlergias(String alergias) { this.alergias = alergias; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public Long getResponsavelId() { return responsavelId; }
    public void setResponsavelId(Long responsavelId) { this.responsavelId = responsavelId; }
}
