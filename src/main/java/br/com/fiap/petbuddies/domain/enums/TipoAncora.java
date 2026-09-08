package br.com.fiap.petbuddies.domain.enums;

/**
 * Data-base a partir da qual o deslocamento do evento e contado.
 * INSTANCIACAO usa o dia em que o plano nasce, NASCIMENTO usa a data de
 * nascimento do animal, DATA_CIRURGIA usa a data de realizacao do procedimento.
 */
public enum TipoAncora {
    INSTANCIACAO,
    NASCIMENTO,
    DATA_CIRURGIA
}
