package br.com.fiap.petbuddies.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCrmv() { return crmv; }
    public void setCrmv(String crmv) { this.crmv = crmv; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    public Long getClinicaId() { return clinicaId; }
    public void setClinicaId(Long clinicaId) { this.clinicaId = clinicaId; }
}
