package br.com.fiap.petbuddies.dto;

import br.com.fiap.petbuddies.domain.entity.ClinicaEntity;
import java.time.LocalDateTime;

public class ClinicaResponse {

    private Long id;
    private String nome;
    private String cnpj;
    private String telefone;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ClinicaResponse from(ClinicaEntity entity) {
        ClinicaResponse dto = new ClinicaResponse();
        dto.id = entity.getId();
        dto.nome = entity.getNome();
        dto.cnpj = entity.getCnpj();
        dto.telefone = entity.getTelefone();
        dto.email = entity.getEmail();
        dto.createdAt = entity.getCreatedAt();
        dto.updatedAt = entity.getUpdatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getCnpj() { return cnpj; }
    public String getTelefone() { return telefone; }
    public String getEmail() { return email; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
