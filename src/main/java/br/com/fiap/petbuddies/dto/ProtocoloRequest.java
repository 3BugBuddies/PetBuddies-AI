package br.com.fiap.petbuddies.dto;

import br.com.fiap.petbuddies.domain.enums.CategoriaProtocolo;
import br.com.fiap.petbuddies.domain.enums.Especie;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ProtocoloRequest {

    @NotBlank(message = "Nome é obrigatório.")
    private String nome;

    @NotNull(message = "Categoria é obrigatória.")
    private CategoriaProtocolo categoria;

    @NotNull(message = "Espécie é obrigatória.")
    private Especie especie;

    private Boolean ativo;
    private String descricao;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public CategoriaProtocolo getCategoria() { return categoria; }
    public void setCategoria(CategoriaProtocolo categoria) { this.categoria = categoria; }

    public Especie getEspecie() { return especie; }
    public void setEspecie(Especie especie) { this.especie = especie; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
}
