package br.com.fiap.petbuddies.dto.cadastro;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClinicaRequest {

    @NotBlank(message = "Nome é obrigatório.")
    @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres.")
    private String nome;

    @NotBlank(message = "CNPJ é obrigatório.")
    @Pattern(regexp = "\\d{14}", message = "CNPJ deve ter exatamente 14 dígitos, sem pontuação.")
    private String cnpj;

    @NotBlank(message = "Telefone é obrigatório.")
    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres.")
    private String telefone;

    @Email(message = "E-mail inválido.")
    @Size(max = 254, message = "E-mail deve ter no máximo 254 caracteres.")
    private String email;
}
