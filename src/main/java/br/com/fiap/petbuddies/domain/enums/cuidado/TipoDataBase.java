package br.com.fiap.petbuddies.domain.enums.cuidado;

/**
 * Data-base a partir da qual o deslocamento da regra e contado.
 *
 * <p>NASCIMENTO usa a data de nascimento do animal; DATA_CIRURGIA usa a data do
 * procedimento cirurgico, lida por projecao; ULTIMA_REALIZACAO usa a ultima vez
 * que aquele cuidado foi feito neste animal, e e ela que expressa periodicidade
 * ("anual") sem materializar ocorrencia nenhuma (ADR s3-24).</p>
 *
 * <p>INSTANCIACAO saiu no PR-J8: contar a partir do dia em que o plano nasce so
 * fazia sentido quando cadastrar o pet instanciava o plano sozinho.</p>
 */
public enum TipoDataBase {
    NASCIMENTO,
    DATA_CIRURGIA,
    ULTIMA_REALIZACAO
}
