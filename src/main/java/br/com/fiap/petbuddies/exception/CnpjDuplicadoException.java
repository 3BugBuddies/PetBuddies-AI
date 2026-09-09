package br.com.fiap.petbuddies.exception;

/**
 * Conflito de identidade da clínica: o CNPJ já pertence a outra linha.
 *
 * <p>Existe para que a violação de {@code UK_CLINICA_CNPJ} chegue ao cliente
 * como 409 e com mensagem de domínio, em vez de subir como erro de driver e cair
 * no 500 genérico do handler.</p>
 */
public class CnpjDuplicadoException extends RuntimeException {
    public CnpjDuplicadoException(String cnpj) {
        super("Já existe uma clínica cadastrada com o CNPJ: " + cnpj);
    }
}
