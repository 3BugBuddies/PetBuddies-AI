package br.com.fiap.petbuddies.exception;

/** Violação de UK_VETERINARIO_CRMV, recusada antes de chegar ao driver. */
public class CrmvDuplicadoException extends RuntimeException {
    public CrmvDuplicadoException(String crmv) {
        super("Já existe um veterinário cadastrado com o CRMV: " + crmv);
    }
}
