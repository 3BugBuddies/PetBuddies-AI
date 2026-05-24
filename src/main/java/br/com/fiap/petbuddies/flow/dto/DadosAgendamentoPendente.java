package br.com.fiap.petbuddies.flow.dto;

import br.com.fiap.petbuddies.domain.enums.Intencao;

import java.util.ArrayList;
import java.util.List;

public class DadosAgendamentoPendente {

    private Long responsavelId;
    private Long animalId;
    private String animalNome;
    private List<AnimalResumo> animaisCandidatos = new ArrayList<>();
    private List<JanelaOfertada> janelasOfertadas = new ArrayList<>();
    private List<JanelaOfertada> todasJanelas = new ArrayList<>();
    private JanelaOfertada janelaEscolhida;
    private Intencao fluxoRetorno;

    public DadosAgendamentoPendente() {}

    public Long getResponsavelId() { return responsavelId; }
    public void setResponsavelId(Long responsavelId) { this.responsavelId = responsavelId; }

    public Long getAnimalId() { return animalId; }
    public void setAnimalId(Long animalId) { this.animalId = animalId; }

    public String getAnimalNome() { return animalNome; }
    public void setAnimalNome(String animalNome) { this.animalNome = animalNome; }

    public List<AnimalResumo> getAnimaisCandidatos() { return animaisCandidatos; }
    public void setAnimaisCandidatos(List<AnimalResumo> animaisCandidatos) { this.animaisCandidatos = animaisCandidatos; }

    public List<JanelaOfertada> getJanelasOfertadas() { return janelasOfertadas; }
    public void setJanelasOfertadas(List<JanelaOfertada> janelasOfertadas) { this.janelasOfertadas = janelasOfertadas; }

    public List<JanelaOfertada> getTodasJanelas() { return todasJanelas; }
    public void setTodasJanelas(List<JanelaOfertada> todasJanelas) { this.todasJanelas = todasJanelas; }

    public JanelaOfertada getJanelaEscolhida() { return janelaEscolhida; }
    public void setJanelaEscolhida(JanelaOfertada janelaEscolhida) { this.janelaEscolhida = janelaEscolhida; }

    public Intencao getFluxoRetorno() { return fluxoRetorno; }
    public void setFluxoRetorno(Intencao fluxoRetorno) { this.fluxoRetorno = fluxoRetorno; }
}
