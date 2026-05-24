package br.com.fiap.petbuddies.flow.dto;

import java.util.ArrayList;
import java.util.List;

public class DadosConsultaPlanoPendente {

    private Long responsavelId;
    private List<AnimalResumo> animaisCandidatos = new ArrayList<>();

    public DadosConsultaPlanoPendente() {}

    public Long getResponsavelId() { return responsavelId; }
    public void setResponsavelId(Long responsavelId) { this.responsavelId = responsavelId; }

    public List<AnimalResumo> getAnimaisCandidatos() { return animaisCandidatos; }
    public void setAnimaisCandidatos(List<AnimalResumo> animaisCandidatos) { this.animaisCandidatos = animaisCandidatos; }
}
