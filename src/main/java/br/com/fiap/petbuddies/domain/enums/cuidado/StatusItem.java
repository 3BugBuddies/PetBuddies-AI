package br.com.fiap.petbuddies.domain.enums.cuidado;

/**
 * Estado de um item do plano. Nao confundir com {@link StatusPlano}, que descreve
 * o curso inteiro: os dois compartilham CANCELADO, entao trocar um pelo outro
 * compila.
 */
public enum StatusItem {
    PENDENTE, REALIZADO, CANCELADO, ATRASADO
}
