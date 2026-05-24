package br.com.fiap.petbuddies.flow.dto;

public class AnimalResumo {

    private Long animalId;
    private String nome;
    private String especie;

    public AnimalResumo() {}

    public Long getAnimalId() { return animalId; }
    public void setAnimalId(Long animalId) { this.animalId = animalId; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEspecie() { return especie; }
    public void setEspecie(String especie) { this.especie = especie; }
}
