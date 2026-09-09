package br.com.fiap.petbuddies.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResponsavelRequest {

    @NotBlank(message = "Nome é obrigatório.")
    @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres.")
    private String nome;

    /** Obrigatório, e deliberadamente não único: dois tutores podem dividir o número. */
    @NotBlank(message = "Telefone é obrigatório.")
    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres.")
    private String telefone;

    @Email(message = "E-mail inválido.")
    @Size(max = 254, message = "E-mail deve ter no máximo 254 caracteres.")
    private String email;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
