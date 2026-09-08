package br.com.fiap.petbuddies.config.persistence;

import jakarta.persistence.EntityManagerFactory;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * O contexto de leitura do registro clinico: a projecao sobre as tabelas que o
 * servico .NET escreve, no mesmo Oracle e no mesmo schema (ADR s3-05).
 *
 * <p>Ele existe separado do cuidado por causa do {@code validate}: a validacao
 * confere TUDO que o {@code EntityManager} mapeia. Se as entidades de leitura
 * estivessem no contexto primario, o Java exigiria que o proprio baseline
 * criasse {@code T_PB_ANIMAL}, que e do .NET, e recusaria subir por causa de
 * tabela que nao e dele. Aqui o schema fica fora da validacao —
 * {@code hbm2ddl.auto = none}, fixo em codigo, e nao herdado.</p>
 *
 * <p>A fonte de dados e a mesma do cuidado. O gerenciador de transacao e
 * proprio e nomeado: quem for transacional sobre esta projecao precisa
 * nomea-lo, porque o primario e o do cuidado.</p>
 */
@Configuration
@EnableJpaRepositories(
        basePackages = "br.com.fiap.petbuddies.domain.readonly.repository",
        entityManagerFactoryRef = "registroEntityManagerFactory",
        transactionManagerRef = "registroTransactionManager")
public class RegistroPersistenceConfig {

    static final String PACOTE_ENTIDADES = "br.com.fiap.petbuddies.domain.readonly";

    @Bean
    public LocalContainerEntityManagerFactoryBean registroEntityManagerFactory(
            EntityManagerFactoryBuilder builder, DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages(PACOTE_ENTIDADES)
                .persistenceUnit("registro")
                .properties(Map.of("hibernate.hbm2ddl.auto", "none"))
                .build();
    }

    @Bean
    public PlatformTransactionManager registroTransactionManager(
            @Qualifier("registroEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
