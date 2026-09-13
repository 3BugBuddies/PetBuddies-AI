package br.com.fiap.petbuddies.dto.cadastro;

import br.com.fiap.petbuddies.domain.entity.VeterinarioEntity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VeterinarioRequest {

    @NotBlank(message = "Nome é obrigatório.")
    @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres.")
    private String nome;

    @NotBlank(message = "CRMV é obrigatório.")
    @Size(max = 30, message = "CRMV deve ter no máximo 30 caracteres.")
    private String crmv;

    @Email(message = "E-mail inválido.")
    @Size(max = 254, message = "E-mail deve ter no máximo 254 caracteres.")
    private String email;

    private Boolean ativo;

    @NotNull(message = "Clínica é obrigatória.")
    private Long clinicaId;

    public static VeterinarioRequest from(VeterinarioEntity entity) {
        VeterinarioRequest request = new VeterinarioRequest();
        request.setNome(entity.getNome());
        request.setCrmv(entity.getCrmv());
        request.setEmail(entity.getEmail());
        request.setAtivo(entity.isAtivo());
        request.setClinicaId(entity.getClinica().getId());
        return request;
    }
}
