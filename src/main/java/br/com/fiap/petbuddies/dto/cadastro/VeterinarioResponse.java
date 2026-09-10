package br.com.fiap.petbuddies.dto.cadastro;

import br.com.fiap.petbuddies.domain.entity.VeterinarioEntity;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VeterinarioResponse {

    private Long id;
    private String nome;
    private String crmv;
    private String email;
    private Boolean ativo;
    private Long clinicaId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static VeterinarioResponse from(VeterinarioEntity entity) {
        VeterinarioResponse dto = new VeterinarioResponse();
        dto.id = entity.getId();
        dto.nome = entity.getNome();
        dto.crmv = entity.getCrmv();
        dto.email = entity.getEmail();
        dto.ativo = entity.isAtivo();
        // Só o id: com open-in-view=false, ler outro campo do proxy LAZY aqui lança LazyInitializationException.
        dto.clinicaId = entity.getClinica() == null ? null : entity.getClinica().getId();
        dto.createdAt = entity.getCreatedAt();
        dto.updatedAt = entity.getUpdatedAt();
        return dto;
    }
}
