package cm.mvtech.drivehub.configs;

import cm.mvtech.drivehub.core.infrastructure.SchemaMultiTenantConnectionProvider;
import cm.mvtech.drivehub.core.infrastructure.TenantIdentifierResolver;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * classe de configuration de l'architecture mutltitenant
 */
@Configuration
public class HibernateMultiTenantConfig {

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer(
            SchemaMultiTenantConnectionProvider connectionProvider,
            TenantIdentifierResolver tenantIdentifierResolver) {

        return properties -> {
            properties.put(
                    "hibernate.multi_tenancy",
                    "SCHEMA"
            );
            properties.put(
                    "hibernate.multi_tenant_connection_provider",
                    connectionProvider
            );
            properties.put(
                    "hibernate.tenant_identifier_resolver",
                    tenantIdentifierResolver
            );
        };
    }
}

