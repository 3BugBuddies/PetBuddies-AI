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
 * O unico contexto de persistencia: as tabelas de que o Java e dono — que a
 * partir do PR-J11 (ADR s3-25) sao TODAS as que ele enxerga.
 *
 * <p>Nao define {@code hibernate.hbm2ddl.auto} em codigo — herda o
 * {@code spring.jpa.hibernate.ddl-auto=validate} do
 * {@code application.properties}. Assim a validacao pode ser desligada por
 * propriedade quando nao ha banco (fumaca de contexto) sem editar esta
 * classe.</p>
 *
 * <p>O segundo contexto, o do registro lido por projecao, deixou de existir
 * junto com {@code domain/readonly}: o Java absorveu as onze tabelas do
 * registro e nao le mais o schema de ninguem. Declarar um
 * {@code EntityManagerFactory} proprio ja desliga a configuracao automatica de
 * persistencia, entao todo repositorio precisa continuar sob o pacote declarado
 * aqui — repositorio fora dele simplesmente nao e criado, e o erro aparece como
 * dependencia nao encontrada na subida.</p>
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
