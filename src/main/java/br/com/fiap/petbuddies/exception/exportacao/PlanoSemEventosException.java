package br.com.fiap.petbuddies.exception.exportacao;

import br.com.fiap.petbuddies.exception.ConflitoException;

// ORA-20023 do PKG_PB_JSON.PLANO_JSON: um plano sem itens, sinal de instanciação que parou no meio.
public class PlanoSemEventosException extends ConflitoException {

    public PlanoSemEventosException(String mensagemDoBanco) {
        super("PLANO_SEM_EVENTOS", mensagemDoBanco);
    }
}
