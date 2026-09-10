package br.com.fiap.petbuddies.domain.enums.cuidado;

/**
 * Filtro de aplicacao do protocolo: que moldes o veterinario ve quando quer
 * aplicar um preventivo ou um pos-cirurgico.
 *
 * <p>Nao admite TRATAMENTO de proposito — tratamento nasce de prescricao
 * assinada, nao de politica da clinica, e portanto nao tem molde. Ver
 * {@link CategoriaPlano}.</p>
 */
public enum CategoriaProtocolo {
    PREVENTIVO, POS_CIRURGICO
}
