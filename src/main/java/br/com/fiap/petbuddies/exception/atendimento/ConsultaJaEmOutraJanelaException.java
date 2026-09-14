package br.com.fiap.petbuddies.exception.atendimento;

import br.com.fiap.petbuddies.exception.ConflitoException;

/** UK_JANELA_VET_INICIO não cobre isto: a consulta só pode ocupar uma janela por vez. */
public class ConsultaJaEmOutraJanelaException extends ConflitoException {
    public ConsultaJaEmOutraJanelaException(Long consultaId) {
        super("CONSULTA_JA_EM_OUTRA_JANELA", "Consulta " + consultaId + " já ocupa outra janela de atendimento.");
    }
}
