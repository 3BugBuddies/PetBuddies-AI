package br.com.fiap.petbuddies.exception.atendimento;

import br.com.fiap.petbuddies.exception.ConflitoException;

/** Recusa a exclusão da consulta com vínculo ativo (FK_REGATEND_CONSULTA e FK_PLANO_CONSULTA). */
public class ConsultaComVinculosException extends ConflitoException {
    public ConsultaComVinculosException(Long id, String vinculo) {
        super("CONSULTA_COM_VINCULOS", "Consulta " + id + " não pode ser removida: possui " + vinculo + ".");
    }
}
