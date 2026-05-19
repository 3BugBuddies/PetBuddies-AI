package br.com.fiap.petbuddies.dto.client;

import br.com.fiap.petbuddies.domain.enums.Especie;
import br.com.fiap.petbuddies.domain.enums.Porte;
import br.com.fiap.petbuddies.domain.enums.Sexo;
import java.time.LocalDate;

public class AnimalMotorDto {

    private Long id;
    private String nome;
    private LocalDate dataNascimento;
    private Boolean condicaoCronica;
    private Boolean castrado;
    private Boolean preCadastro;
    private Sexo sexo;
    private Especie especie;
    private Porte porte;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate v) { this.dataNascimento = v; }

    public Boolean getCondicaoCronica() { return condicaoCronica; }
    public void setCondicaoCronica(Boolean v) { this.condicaoCronica = v; }

    public Boolean getCastrado() { return castrado; }
    public void setCastrado(Boolean castrado) { this.castrado = castrado; }

    public Boolean getPreCadastro() { return preCadastro; }
    public void setPreCadastro(Boolean preCadastro) { this.preCadastro = preCadastro; }

    public Sexo getSexo() { return sexo; }
    public void setSexo(Sexo sexo) { this.sexo = sexo; }

    public Especie getEspecie() { return especie; }
    public void setEspecie(Especie especie) { this.especie = especie; }

    public Porte getPorte() { return porte; }
    public void setPorte(Porte porte) { this.porte = porte; }
}
