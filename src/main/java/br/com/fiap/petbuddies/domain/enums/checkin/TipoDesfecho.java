package br.com.fiap.petbuddies.domain.enums.checkin;

/**
 * O que o motor decidiu para um item de tratamento num dia. Nulo em item de
 * origem PROTOCOLO — vacina nao tem dose, e por isso CK_ITEM_DESFECHO_DOSE e
 * ternario e nao binario.
 */
public enum TipoDesfecho {
    DOSE_CALCULADA, ACIONAR_CLINICA, SEM_DOSE
}
