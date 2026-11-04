package br.com.fiap.petbuddies.exception.exportacao;

import br.com.fiap.petbuddies.exception.RecursoNaoEncontradoException;

// ORA-20022 (o animal não tem plano) e ORA-20024 (nenhum animal tem plano) do PKG_PB_JSON.PLANO_JSON.
public class DatasetSemPlanoException extends RecursoNaoEncontradoException {

    public DatasetSemPlanoException(String mensagemDoBanco) {
        super("DATASET_SEM_PLANO", mensagemDoBanco);
    }
}
