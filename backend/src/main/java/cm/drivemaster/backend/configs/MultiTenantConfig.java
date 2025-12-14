package cm.drivemaster.backend.configs;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;


/**
 * classe de configuration de l'architecture mutltitenant
 */
@Configuration
@EnableJpaRepositories
@EnableTransactionManagement // autorisé la gestion des transaction garantit que les opération de bases sont automatiques(commit/rollback)
public class MultiTenantConfig {


    /**
     * creation d'un dataSource personalisé
     * contenant la logique du multitenant
     * @return mutlTenantDataSourceObject
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:postgresql://localhost:5432/ges_auto_ecoles");
        ds.setUsername("postgres");
        ds.setPassword("root");
        return ds;
    }


    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {

        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        // associer l'EntityManager au dataSource qui choisit le multiTenant
        factoryBean.setDataSource(dataSource());
        // definir ou sont stocker les entité JPA
        factoryBean.setPackagesToScan("cm.drivemaster.backend.beans");
        // definir hibernate comme moteur JPA
        factoryBean.setJpaVendorAdapter(
                new HibernateJpaVendorAdapter()
        );

        Properties props = getProperties();

        factoryBean.setJpaProperties(props);
        return  factoryBean;
    }

    /**
     * setup des Proprietés Hibernate, JPA pour le mutltitenancy
     * @return
     */
    private static Properties getProperties() {
        Properties props = new Properties();
        // active le mutltitenancy par schema
        props.setProperty("hibernate.multiTenancy", "SCHEMA");

        // permet d'identitfer le tenant courant(JWT, header HTTP)
        props.setProperty(
                "hibernate.tenant_identifier_resolver",
                "com.example.config.TenantIdentifierResolver"
        );

        // fourni la connexion SQL en fonction du tenant identifié
        props.setProperty(
                "hibernate.multi_tenant_connection_provider",
                "com.example.config.MultiTenantConnectionProvider"
        );
        return props;
    }
}
