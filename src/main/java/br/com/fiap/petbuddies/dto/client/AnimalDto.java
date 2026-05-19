package br.com.fiap.petbuddies.dto.client;

public class AnimalDto {

    private Long id;
    private String nome;
    private String especie;
    private String porte;
    private String sexo;
    private Boolean castrado;
    private Boolean preCadastro;

    public AnimalDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEspecie() { return especie; }
    public void setEspecie(String especie) { this.especie = especie; }

    public String getPorte() { return porte; }
    public void setPorte(String porte) { this.porte = porte; }

    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }

    public Boolean getCastrado() { return castrado; }
    public void setCastrado(Boolean castrado) { this.castrado = castrado; }

    public Boolean getPreCadastro() { return preCadastro; }
    public void setPreCadastro(Boolean preCadastro) { this.preCadastro = preCadastro; }
}
