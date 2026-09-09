package br.com.fiap.petbuddies.dto;

import br.com.fiap.petbuddies.domain.entity.ProtocoloEntity;
import br.com.fiap.petbuddies.domain.enums.CategoriaProtocolo;
import br.com.fiap.petbuddies.domain.enums.Especie;
import java.time.LocalDateTime;

public class ProtocoloResponse {

    private Long id;
    private String nome;
    private CategoriaProtocolo categoria;
    private Especie especie;
    private boolean ativo;
    private String descricao;
    private LocalDateTime createdAt;

    public static ProtocoloResponse from(ProtocoloEntity entity) {
        ProtocoloResponse dto = new ProtocoloResponse();
        dto.id = entity.getId();
        dto.nome = entity.getNome();
        dto.categoria = entity.getCategoria();
        dto.especie = entity.getEspecie();
        dto.ativo = entity.isAtivo();
        dto.descricao = entity.getDescricao();
        dto.createdAt = entity.getCreatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public CategoriaProtocolo getCategoria() { return categoria; }
    public Especie getEspecie() { return especie; }
    public boolean isAtivo() { return ativo; }
    public String getDescricao() { return descricao; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
