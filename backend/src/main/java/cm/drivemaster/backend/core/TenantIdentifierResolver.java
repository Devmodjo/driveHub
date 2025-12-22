package cm.drivemaster.backend.core;


import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Cette classe permet à Hibernate de savoir, à chaque requête,
 * dans quel schéma il doit exécuter le SQL.
 */
@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver {

//    @Override
//    public String resolveCurrentTenantIdentifier() {
//        String tenantId = TenantContext.getTenantId();
//        return tenantId != null ? tenantId : "default";
//    }

    @Override
    public String resolveCurrentTenantIdentifier() {
        return Optional.ofNullable(TenantContext.getTenantId())
                .orElse("public");
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
