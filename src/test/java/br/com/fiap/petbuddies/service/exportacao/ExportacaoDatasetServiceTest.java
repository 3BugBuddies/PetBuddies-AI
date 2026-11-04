package br.com.fiap.petbuddies.service.exportacao;

import br.com.fiap.petbuddies.exception.cadastro.AnimalNaoEncontradoException;
import br.com.fiap.petbuddies.exception.exportacao.DatasetSemPlanoException;
import br.com.fiap.petbuddies.exception.exportacao.PlanoSemEventosException;
import br.com.fiap.petbuddies.infrastructure.banco.PlanoJsonProcedure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.UncategorizedSQLException;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExportacaoDatasetServiceTest {

    private PlanoJsonProcedure procedure;
    private ExportacaoDatasetService service;

    // Como o driver do Oracle entrega um RAISE_APPLICATION_ERROR: código positivo,
    // mensagem com o ORA na frente e a pilha do PL/SQL nas linhas seguintes.
    private static UncategorizedSQLException erroDoOracle(int codigo, String mensagem) {
        SQLException sql = new SQLException(
                "ORA-" + codigo + ": " + mensagem + "\nORA-06512: em \"PKG_PB_JSON\", line 412",
                "72000", codigo);
        return new UncategorizedSQLException("PLANO_JSON", "{call PKG_PB_JSON.PLANO_JSON(?, ?)}", sql);
    }

    @BeforeEach
    void preparar() {
        procedure = mock(PlanoJsonProcedure.class);
        service = new ExportacaoDatasetService(procedure);
    }

    @Test
    void planosCuidado_procedureResponde_devolveOJsonDoBanco() {
        when(procedure.executar(null)).thenReturn("[{\"planoId\":1,\"eventos\":[]}]");

        assertEquals("[{\"planoId\":1,\"eventos\":[]}]", service.planosCuidado(null));
    }

    @Test
    void planosCuidado_animalInexistente_vira404DeAnimal() {
        when(procedure.executar(9999L)).thenThrow(erroDoOracle(20021, "PLANO_JSON: animal 9999 não existe."));

        assertThrows(AnimalNaoEncontradoException.class, () -> service.planosCuidado(9999L));
    }

    @Test
    void planosCuidado_animalSemPlano_vira404ComAMensagemDoBanco() {
        when(procedure.executar(6L)).thenThrow(erroDoOracle(20022,
                "PLANO_JSON: o animal 6 (Pet sem plano) existe, mas não tem plano de cuidado."));

        DatasetSemPlanoException erro = assertThrows(DatasetSemPlanoException.class, () -> service.planosCuidado(6L));
        assertEquals("PLANO_JSON: o animal 6 (Pet sem plano) existe, mas não tem plano de cuidado.", erro.getMessage());
    }

    @Test
    void planosCuidado_planoSemItens_vira409() {
        when(procedure.executar(1L)).thenThrow(erroDoOracle(20023, "PLANO_JSON: plano 7 do animal 1 não tem nenhum evento."));

        assertThrows(PlanoSemEventosException.class, () -> service.planosCuidado(1L));
    }

    @Test
    void planosCuidado_nenhumPlanoNoBanco_vira404() {
        when(procedure.executar(null)).thenThrow(erroDoOracle(20024, "PLANO_JSON: nenhum animal tem plano de cuidado."));

        assertThrows(DatasetSemPlanoException.class, () -> service.planosCuidado(null));
    }

    @Test
    void planosCuidado_erroQueNaoEDoProcedimento_sobeComoEsta() {
        UncategorizedSQLException pacoteInvalido = erroDoOracle(4063, "package body \"PKG_PB_JSON\" has errors");
        when(procedure.executar(null)).thenThrow(pacoteInvalido);

        assertSame(pacoteInvalido, assertThrows(UncategorizedSQLException.class, () -> service.planosCuidado(null)));
    }
}
