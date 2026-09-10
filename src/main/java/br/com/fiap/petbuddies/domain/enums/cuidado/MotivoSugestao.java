package br.com.fiap.petbuddies.domain.enums.cuidado;

/** Por que um cuidado aparece na sugestão por histórico (PR-J9). */
public enum MotivoSugestao {
    /** Item já materializado, {@code PENDENTE} ou {@code ATRASADO}, com data-alvo no passado. */
    REFORCO_VENCIDO,
    /** Regra ancorada em {@code ULTIMA_REALIZACAO}: o animal já fez, e o intervalo venceu. */
    RECORRENCIA_DEVIDA,
    /** Regra ancorada em {@code ULTIMA_REALIZACAO} que o animal nunca recebeu. */
    NUNCA_REALIZADO
}
