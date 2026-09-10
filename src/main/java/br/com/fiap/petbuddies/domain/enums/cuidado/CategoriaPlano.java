package br.com.fiap.petbuddies.domain.enums.cuidado;

/**
 * O que um curso de cuidado e. Propria do plano, e nao herdada do protocolo
 * (ADR s3-24 §4b): plano de tratamento nasce de prescricao e nao tem molde.
 *
 * <p>Ao aplicar um protocolo, a categoria dele e COPIADA para o plano — e por
 * isso {@link CategoriaProtocolo} nao admite TRATAMENTO.</p>
 */
public enum CategoriaPlano {
    PREVENTIVO, POS_CIRURGICO, TRATAMENTO
}
