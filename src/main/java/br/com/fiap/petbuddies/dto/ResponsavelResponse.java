package br.com.fiap.petbuddies.dto;

import br.com.fiap.petbuddies.domain.entity.ResponsavelEntity;
import java.time.LocalDateTime;

public class ResponsavelResponse {

    private Long id;
    private String nome;
    private String telefone;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ResponsavelResponse from(ResponsavelEntity entity) {
        ResponsavelResponse dto = new ResponsavelResponse();
        dto.id = entity.getId();
        dto.nome = entity.getNome();
        dto.telefone = entity.getTelefone();
        dto.email = entity.getEmail();
        dto.createdAt = entity.getCreatedAt();
        dto.updatedAt = entity.getUpdatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getTelefone() { return telefone; }
    public String getEmail() { return email; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
