package br.com.fiap.petbuddies.dto.client;

public class CadastrarResponsavelRequest {

    private String nome;
    private String telefone;

    public CadastrarResponsavelRequest() {}

    public CadastrarResponsavelRequest(String nome, String telefone) {
        this.nome = nome;
        this.telefone = telefone;
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
}
