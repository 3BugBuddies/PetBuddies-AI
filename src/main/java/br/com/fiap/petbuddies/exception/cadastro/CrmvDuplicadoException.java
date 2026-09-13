package br.com.fiap.petbuddies.exception.cadastro;

import br.com.fiap.petbuddies.exception.ConflitoException;

/** Violação de UK_VETERINARIO_CRMV, recusada antes de chegar ao driver. */
public class CrmvDuplicadoException extends ConflitoException {
    public CrmvDuplicadoException(String crmv) {
        super("CRMV_DUPLICADO", "Já existe um veterinário cadastrado com o CRMV: " + crmv);
    }
}
