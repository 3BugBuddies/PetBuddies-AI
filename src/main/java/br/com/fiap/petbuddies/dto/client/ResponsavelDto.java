package br.com.fiap.petbuddies.dto.client;

public class ResponsavelDto {

    private Long id;
    private String nome;
    private String telefone;
    private String status;

    public ResponsavelDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
