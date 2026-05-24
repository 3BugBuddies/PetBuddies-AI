package br.com.fiap.petbuddies.dto.protocolo;

import br.com.fiap.petbuddies.domain.enums.CategoriaProtocolo;
import br.com.fiap.petbuddies.domain.enums.Especie;
import br.com.fiap.petbuddies.domain.enums.Porte;
import br.com.fiap.petbuddies.domain.enums.Sexo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ProtocoloRequest {

    @NotBlank(message = "Nome é obrigatório.")
    private String nome;

    @NotNull(message = "Categoria é obrigatória.")
    private CategoriaProtocolo categoria;

    @NotNull(message = "Espécie é obrigatória.")
    private Especie especie;

    private Porte porte;
    private Sexo sexo;
    private Boolean castrado;
    private Boolean ativo;
    private Integer idadeMinMeses;
    private Integer idadeMaxMeses;
    private String descricao;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public CategoriaProtocolo getCategoria() { return categoria; }
    public void setCategoria(CategoriaProtocolo categoria) { this.categoria = categoria; }

    public Especie getEspecie() { return especie; }
    public void setEspecie(Especie especie) { this.especie = especie; }

    public Porte getPorte() { return porte; }
    public void setPorte(Porte porte) { this.porte = porte; }

    public Sexo getSexo() { return sexo; }
    public void setSexo(Sexo sexo) { this.sexo = sexo; }

    public Boolean getCastrado() { return castrado; }
    public void setCastrado(Boolean castrado) { this.castrado = castrado; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    public Integer getIdadeMinMeses() { return idadeMinMeses; }
    public void setIdadeMinMeses(Integer idadeMinMeses) { this.idadeMinMeses = idadeMinMeses; }

    public Integer getIdadeMaxMeses() { return idadeMaxMeses; }
    public void setIdadeMaxMeses(Integer idadeMaxMeses) { this.idadeMaxMeses = idadeMaxMeses; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
}
