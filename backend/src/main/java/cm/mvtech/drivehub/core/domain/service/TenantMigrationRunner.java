package cm.mvtech.drivehub.core.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Au démarrage de l'application, met à jour le schéma de chaque auto-école
 * avec les nouvelles migrations de {@code db/migration/tenant/}.
 */
@Component
@RequiredArgsConstructor
public class TenantMigrationRunner implements ApplicationRunner {

    private final TenantProvisioningService provisioningService;

    @Override
    public void run(ApplicationArguments args) {
        provisioningService.migrateAllTenants();
    }
}
