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

/**
 * O contexto primario: as tabelas de que o Java e dono, e as unicas que ele
 * valida.
 *
 * <p>Nao define {@code hibernate.hbm2ddl.auto} em codigo — herda o
 * {@code spring.jpa.hibernate.ddl-auto=validate} do
 * {@code application.properties}. Assim a validacao pode ser desligada por
 * propriedade quando nao ha banco (fumaca de contexto) sem editar esta
 * classe.</p>
 *
 * <p>Existir um segundo contexto desliga a configuracao automatica de
 * persistencia. A partir daqui todo repositorio precisa estar sob um dos dois
 * pacotes declarados — repositorio fora deles simplesmente nao e criado, e o
 * erro aparece como dependencia nao encontrada na subida.</p>
 */
@Configuration
@EnableJpaRepositories(
        basePackages = "br.com.fiap.petbuddies.domain.repository",
        entityManagerFactoryRef = "cuidadoEntityManagerFactory",
        transactionManagerRef = "cuidadoTransactionManager")
public class CuidadoPersistenceConfig {

    static final String PACOTE_ENTIDADES = "br.com.fiap.petbuddies.domain.entity";

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
