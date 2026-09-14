package br.com.fiap.petbuddies.exception.atendimento;

import br.com.fiap.petbuddies.exception.ConflitoException;

/** Recusa a exclusão do registro de atendimento com vínculo ativo (FK_PROCED_REGATEND e FK_PRESCRICAO_REGATEND). */
public class RegistroAtendimentoComVinculosException extends ConflitoException {
    public RegistroAtendimentoComVinculosException(Long id, String vinculo) {
        super("REGISTRO_ATENDIMENTO_COM_VINCULOS",
                "Registro de atendimento " + id + " não pode ser removido: possui " + vinculo + ".");
    }
}
