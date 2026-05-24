package br.com.fiap.petbuddies.dto.protocolo;

import br.com.fiap.petbuddies.domain.entity.ProtocoloEntity;
import br.com.fiap.petbuddies.domain.enums.CategoriaProtocolo;
import br.com.fiap.petbuddies.domain.enums.Especie;
import br.com.fiap.petbuddies.domain.enums.Porte;
import br.com.fiap.petbuddies.domain.enums.Sexo;
import java.time.LocalDateTime;

public class ProtocoloResponse {

    private Long id;
    private String nome;
    private CategoriaProtocolo categoria;
    private Especie especie;
    private Porte porte;
    private Sexo sexo;
    private Boolean castrado;
    private boolean ativo;
    private Integer idadeMinMeses;
    private Integer idadeMaxMeses;
    private String descricao;
    private LocalDateTime createdAt;

    public static ProtocoloResponse from(ProtocoloEntity entity) {
        ProtocoloResponse dto = new ProtocoloResponse();
        dto.id = entity.getId();
        dto.nome = entity.getNome();
        dto.categoria = entity.getCategoria();
        dto.especie = entity.getEspecie();
        dto.porte = entity.getPorte();
        dto.sexo = entity.getSexo();
        dto.castrado = entity.getCastrado();
        dto.ativo = entity.isAtivo();
        dto.idadeMinMeses = entity.getIdadeMinMeses();
        dto.idadeMaxMeses = entity.getIdadeMaxMeses();
        dto.descricao = entity.getDescricao();
        dto.createdAt = entity.getCreatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public CategoriaProtocolo getCategoria() { return categoria; }
    public Especie getEspecie() { return especie; }
    public Porte getPorte() { return porte; }
    public Sexo getSexo() { return sexo; }
    public Boolean getCastrado() { return castrado; }
    public boolean isAtivo() { return ativo; }
    public Integer getIdadeMinMeses() { return idadeMinMeses; }
    public Integer getIdadeMaxMeses() { return idadeMaxMeses; }
    public String getDescricao() { return descricao; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
