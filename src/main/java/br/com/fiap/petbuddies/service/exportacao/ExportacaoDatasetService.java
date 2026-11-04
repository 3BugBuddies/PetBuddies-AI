package br.com.fiap.petbuddies.service.exportacao;

import br.com.fiap.petbuddies.exception.cadastro.AnimalNaoEncontradoException;
import br.com.fiap.petbuddies.exception.exportacao.DatasetSemPlanoException;
import br.com.fiap.petbuddies.exception.exportacao.PlanoSemEventosException;
import br.com.fiap.petbuddies.infrastructure.banco.PlanoJsonProcedure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

/**
 * Exporta os planos de cuidado em JSON pelo procedimento PKG_PB_JSON.PLANO_JSON,
 * no formato que a Sprint 4 importa no MongoDB: um documento por plano, com os
 * eventos embutidos.
 */
@Service
public class ExportacaoDatasetService {

    private static final Logger log = LoggerFactory.getLogger(ExportacaoDatasetService.class);

    private final PlanoJsonProcedure planoJsonProcedure;

    public ExportacaoDatasetService(PlanoJsonProcedure planoJsonProcedure) {
        this.planoJsonProcedure = planoJsonProcedure;
    }

    public String planosCuidado(Long animalId) {
        try {
            String json = planoJsonProcedure.executar(animalId);
            log.info("[DATASET] PKG_PB_JSON.PLANO_JSON executada (animalId={}): {} caracteres",
                    animalId == null ? "todos" : animalId, json.length());
            return json;
        } catch (DataAccessException e) {
            // As exceções nomeadas do procedimento viram os erros da API; o código
            // ORA chega positivo em getErrorCode.
            SQLException erro = sqlException(e);
            int codigo = erro == null ? 0 : erro.getErrorCode();
            switch (codigo) {
                case 20021 -> throw new AnimalNaoEncontradoException(animalId);
                case 20022, 20024 -> throw new DatasetSemPlanoException(mensagemDoBanco(erro));
                case 20023 -> throw new PlanoSemEventosException(mensagemDoBanco(erro));
                default -> throw e;
            }
        }
    }

    private static SQLException sqlException(Throwable erro) {
        for (Throwable causa = erro; causa != null; causa = causa.getCause()) {
            if (causa instanceof SQLException sql) {
                return sql;
            }
        }
        return null;
    }

    // "ORA-20022: PLANO_JSON: o animal 6 ...\nORA-06512: ..." -> "PLANO_JSON: o animal 6 ..."
    private static String mensagemDoBanco(SQLException erro) {
        String primeiraLinha = erro.getMessage().lines().findFirst().orElse("");
        return primeiraLinha.replaceFirst("^ORA-\\d+:\\s*", "");
    }
}
