package br.com.fiap.petbuddies.dto;

import br.com.fiap.petbuddies.domain.entity.ClinicaEntity;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
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
}
