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
            // Hibernate 6 : le multi-tenant est activé dès qu'un MultiTenantConnectionProvider est fourni
            // (l'ancienne propriété "hibernate.multi_tenancy" n'existe plus et était ignorée).
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

