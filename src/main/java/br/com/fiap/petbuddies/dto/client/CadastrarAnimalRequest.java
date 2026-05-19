package br.com.fiap.petbuddies.dto.client;

public class CadastrarAnimalRequest {

    private Long responsavelId;
    private String nome;
    private String especie;
    private String porte;
    private String sexo;
    private boolean castrado;
    private String dataNascimento;

    public CadastrarAnimalRequest() {}

    public CadastrarAnimalRequest(Long responsavelId, String nome, String especie,
                                   String porte, String sexo, boolean castrado, String dataNascimento) {
        this.responsavelId = responsavelId;
        this.nome = nome;
        this.especie = especie;
        this.porte = porte;
        this.sexo = sexo;
        this.castrado = castrado;
        this.dataNascimento = dataNascimento;
    }

    public Long getResponsavelId() { return responsavelId; }
    public void setResponsavelId(Long responsavelId) { this.responsavelId = responsavelId; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEspecie() { return especie; }
    public void setEspecie(String especie) { this.especie = especie; }

    public String getPorte() { return porte; }
    public void setPorte(String porte) { this.porte = porte; }

    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }

    public boolean isCastrado() { return castrado; }
    public void setCastrado(boolean castrado) { this.castrado = castrado; }

    public String getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(String dataNascimento) { this.dataNascimento = dataNascimento; }
}
