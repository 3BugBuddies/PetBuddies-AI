package br.com.fiap.petbuddies.exception.atendimento;

import br.com.fiap.petbuddies.exception.ConflitoException;

/** Violação de UK_CONDICAO_CLINICA_CODIGO, recusada antes de chegar ao driver. */
public class CodigoCondicaoDuplicadoException extends ConflitoException {
    public CodigoCondicaoDuplicadoException(Long clinicaId, String codigo) {
        super("CODIGO_CONDICAO_DUPLICADO", "A clínica " + clinicaId + " já tem uma condição clínica com o código: " + codigo);
    }
}
