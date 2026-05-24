package br.com.fiap.petbuddies.flow.dto;

import br.com.fiap.petbuddies.domain.enums.Intencao;

public class DadosCadastroPendente {

    private String nomeTutor;
    private Long responsavelId;
    private String nomeAnimal;
    private String especie;
    private String porte;
    private String sexo;
    private Boolean castrado;
    private String dataNascimento;
    private Intencao fluxoRetorno;

    public DadosCadastroPendente() {}

    public String getNomeTutor() { return nomeTutor; }
    public void setNomeTutor(String nomeTutor) { this.nomeTutor = nomeTutor; }

    public Long getResponsavelId() { return responsavelId; }
    public void setResponsavelId(Long responsavelId) { this.responsavelId = responsavelId; }

    public String getNomeAnimal() { return nomeAnimal; }
    public void setNomeAnimal(String nomeAnimal) { this.nomeAnimal = nomeAnimal; }

    public String getEspecie() { return especie; }
    public void setEspecie(String especie) { this.especie = especie; }

    public String getPorte() { return porte; }
    public void setPorte(String porte) { this.porte = porte; }

    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }

    public Boolean getCastrado() { return castrado; }
    public void setCastrado(Boolean castrado) { this.castrado = castrado; }

    public String getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(String dataNascimento) { this.dataNascimento = dataNascimento; }

    public Intencao getFluxoRetorno() { return fluxoRetorno; }
    public void setFluxoRetorno(Intencao fluxoRetorno) { this.fluxoRetorno = fluxoRetorno; }
}
