package br.com.fiap.petbuddies.dto.client;

// TODO PRD 04: expandir com especie, porte, dataNascimento, condicaoCronica, castrado, sexo
public class ResponsavelDto {

    private Long id;
    private String nome;
    private String status;

    public ResponsavelDto(Long id, String nome, String status) {
        this.id = id;
        this.nome = nome;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
