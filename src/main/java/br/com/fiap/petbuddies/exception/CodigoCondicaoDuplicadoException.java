package br.com.fiap.petbuddies.exception;

/** Violação de UK_CONDICAO_CLINICA_CODIGO, recusada antes de chegar ao driver. */
public class CodigoCondicaoDuplicadoException extends RuntimeException {
    public CodigoCondicaoDuplicadoException(Long clinicaId, String codigo) {
        super("A clínica " + clinicaId + " já tem uma condição clínica com o código: " + codigo);
    }
}
