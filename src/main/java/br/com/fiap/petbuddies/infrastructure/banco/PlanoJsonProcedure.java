package br.com.fiap.petbuddies.infrastructure.banco;

import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Types;

/**
 * Chama PKG_PB_JSON.PLANO_JSON, instalado pela migration V9 a partir de
 * docs/banco/05_pacotes.sql: o JOIN de animal, plano e itens montado em JSON
 * dentro do Oracle. A JPA não chama procedure com parâmetro OUT de CLOB, por
 * isso a chamada é JDBC direto.
 */
@Component
public class PlanoJsonProcedure {

    private static final String CHAMADA = "{call PKG_PB_JSON.PLANO_JSON(?, ?)}";

    private final JdbcTemplate jdbcTemplate;

    public PlanoJsonProcedure(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** animalId nulo devolve o dataset inteiro: um array com todos os planos. */
    public String executar(Long animalId) {
        return jdbcTemplate.execute((ConnectionCallback<String>) conexao -> {
            try (CallableStatement chamada = conexao.prepareCall(CHAMADA)) {
                if (animalId == null) {
                    chamada.setNull(1, Types.NUMERIC);
                } else {
                    chamada.setLong(1, animalId);
                }
                chamada.registerOutParameter(2, Types.CLOB);
                chamada.execute();

                // O CLOB é temporário no Oracle: lido aqui, com a conexão ainda aberta.
                Clob json = chamada.getClob(2);
                try {
                    return json.getSubString(1, (int) json.length());
                } finally {
                    json.free();
                }
            }
        });
    }
}
