package br.com.fiap.petbuddies.flow.dto;

import br.com.fiap.petbuddies.domain.enums.Intencao;
import br.com.fiap.petbuddies.dto.bot.AtoComunicativo;

public class FlowResponse {

    public enum Tipo {
        TEXTO, FINALIZAR_FLUXO, NOVO_FLUXO
    }

    private Tipo tipo;
    private String texto;
    private Intencao novoFluxo;
    private AtoComunicativo ato;

    public FlowResponse() {}

    public static FlowResponse texto(String texto) {
        FlowResponse response = new FlowResponse();
        response.setTipo(Tipo.TEXTO);
        response.setTexto(texto);
        return response;
    }

    public static FlowResponse finalizarFluxo(String texto) {
        FlowResponse response = new FlowResponse();
        response.setTipo(Tipo.FINALIZAR_FLUXO);
        response.setTexto(texto);
        return response;
    }

    public static FlowResponse novoFluxo(Intencao novoFluxo, String texto) {
        FlowResponse response = new FlowResponse();
        response.setTipo(Tipo.NOVO_FLUXO);
        response.setNovoFluxo(novoFluxo);
        response.setTexto(texto);
        return response;
    }

    public static FlowResponse comAto(AtoComunicativo ato) {
        FlowResponse response = new FlowResponse();
        response.setTipo(Tipo.TEXTO);
        response.setAto(ato);
        response.setTexto(ato != null ? ato.getTextoFallback() : null);
        return response;
    }

    public static FlowResponse finalizarFluxoComAto(AtoComunicativo ato) {
        FlowResponse response = comAto(ato);
        response.setTipo(Tipo.FINALIZAR_FLUXO);
        return response;
    }

    public static FlowResponse novoFluxoComAto(Intencao novoFluxo, AtoComunicativo ato) {
        FlowResponse response = comAto(ato);
        response.setTipo(Tipo.NOVO_FLUXO);
        response.setNovoFluxo(novoFluxo);
        return response;
    }

    public Tipo getTipo() { return tipo; }
    public void setTipo(Tipo tipo) { this.tipo = tipo; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public Intencao getNovoFluxo() { return novoFluxo; }
    public void setNovoFluxo(Intencao novoFluxo) { this.novoFluxo = novoFluxo; }

    public AtoComunicativo getAto() { return ato; }
    public void setAto(AtoComunicativo ato) { this.ato = ato; }
}
