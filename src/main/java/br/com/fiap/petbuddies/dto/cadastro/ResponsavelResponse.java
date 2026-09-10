package br.com.fiap.petbuddies.dto.cadastro;

import br.com.fiap.petbuddies.domain.entity.ResponsavelEntity;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
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
}
