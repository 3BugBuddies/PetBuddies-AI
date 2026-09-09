package br.com.fiap.petbuddies.config.persistence;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

// EntityManagerFactory proprio desliga a auto-config de persistencia: repositorio fora de
// basePackages nao e criado, e o erro aparece como dependencia ausente na subida.
@Configuration
@EnableJpaRepositories(
        basePackages = "br.com.fiap.petbuddies.domain.repository",
        entityManagerFactoryRef = "cuidadoEntityManagerFactory",
        transactionManagerRef = "cuidadoTransactionManager")
public class CuidadoPersistenceConfig {

    static final String PACOTE_ENTIDADES = "br.com.fiap.petbuddies.domain.entity";

    // Sem hibernate.hbm2ddl.auto aqui: herdado de application.properties, pode ser desligado por propriedade.
    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean cuidadoEntityManagerFactory(
            EntityManagerFactoryBuilder builder, DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages(PACOTE_ENTIDADES)
                .persistenceUnit("cuidado")
                .build();
    }

    @Primary
    @Bean
    public PlatformTransactionManager cuidadoTransactionManager(
            @Qualifier("cuidadoEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
