package br.com.fiap.petbuddies.dto.bot;

import java.util.Map;

public class AtoComunicativo {

    public enum Tipo {
        PERGUNTAR,
        INFORMAR_RESULTADO,
        APRESENTAR_OPCOES,
        CONFIRMAR,
        CONFIRMAR_SUCESSO,
        ORIENTAR,
        ORIENTAR_EMERGENCIA
    }

    public enum Liberdade {
        ESTRITA,
        AMPLA
    }

    private Tipo tipo;
    private Liberdade liberdade = Liberdade.ESTRITA;
    private String textoFallback;
    private Map<String, Object> dados;

    public AtoComunicativo() {}

    public static AtoComunicativo perguntar(String fallback, Map<String, Object> dados) {
        return criar(Tipo.PERGUNTAR, fallback, dados, Liberdade.ESTRITA);
    }

    public static AtoComunicativo informarResultado(String fallback, Map<String, Object> dados) {
        return criar(Tipo.INFORMAR_RESULTADO, fallback, dados, Liberdade.ESTRITA);
    }

    public static AtoComunicativo informarResultado(String fallback, Map<String, Object> dados, Liberdade liberdade) {
        return criar(Tipo.INFORMAR_RESULTADO, fallback, dados, liberdade);
    }

    public static AtoComunicativo apresentarOpcoes(String fallback, Map<String, Object> dados) {
        return criar(Tipo.APRESENTAR_OPCOES, fallback, dados, Liberdade.ESTRITA);
    }

    public static AtoComunicativo confirmar(String fallback, Map<String, Object> dados) {
        return criar(Tipo.CONFIRMAR, fallback, dados, Liberdade.ESTRITA);
    }

    public static AtoComunicativo confirmarSucesso(String fallback, Map<String, Object> dados) {
        return criar(Tipo.CONFIRMAR_SUCESSO, fallback, dados, Liberdade.ESTRITA);
    }

    public static AtoComunicativo orientar(String fallback, Map<String, Object> dados) {
        return criar(Tipo.ORIENTAR, fallback, dados, Liberdade.ESTRITA);
    }

    public static AtoComunicativo orientarEmergencia(String fallback, Map<String, Object> dados) {
        return criar(Tipo.ORIENTAR_EMERGENCIA, fallback, dados, Liberdade.ESTRITA);
    }

    private static AtoComunicativo criar(Tipo tipo, String fallback, Map<String, Object> dados, Liberdade liberdade) {
        AtoComunicativo ato = new AtoComunicativo();
        ato.setTipo(tipo);
        ato.setTextoFallback(fallback);
        ato.setDados(dados);
        ato.setLiberdade(liberdade == null ? Liberdade.ESTRITA : liberdade);
        return ato;
    }

    public Tipo getTipo() { return tipo; }
    public void setTipo(Tipo tipo) { this.tipo = tipo; }

    public Liberdade getLiberdade() { return liberdade; }
    public void setLiberdade(Liberdade liberdade) { this.liberdade = liberdade; }

    public String getTextoFallback() { return textoFallback; }
    public void setTextoFallback(String textoFallback) { this.textoFallback = textoFallback; }

    public Map<String, Object> getDados() { return dados; }
    public void setDados(Map<String, Object> dados) { this.dados = dados; }
}
