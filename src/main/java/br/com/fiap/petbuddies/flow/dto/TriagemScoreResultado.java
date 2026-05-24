package br.com.fiap.petbuddies.flow.dto;

import br.com.fiap.petbuddies.domain.enums.ClassificacaoTriagem;

public class TriagemScoreResultado {

    private Integer score;
    private ClassificacaoTriagem classificacao;
    private String recomendacao;

    public TriagemScoreResultado() {}

    public TriagemScoreResultado(Integer score, ClassificacaoTriagem classificacao, String recomendacao) {
        this.score = score;
        this.classificacao = classificacao;
        this.recomendacao = recomendacao;
    }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public ClassificacaoTriagem getClassificacao() { return classificacao; }
    public void setClassificacao(ClassificacaoTriagem classificacao) { this.classificacao = classificacao; }

    public String getRecomendacao() { return recomendacao; }
    public void setRecomendacao(String recomendacao) { this.recomendacao = recomendacao; }
}
