package cm.mvtech.drivehub.core.domain.service;

import cm.mvtech.drivehub.core.domain.entities.TenantEntity;
import cm.mvtech.drivehub.core.infrastructure.TenantSchemas;
import cm.mvtech.drivehub.core.infrastructure.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.List;

/**
 * Création et mise à jour des schémas PostgreSQL des auto-écoles.
 *
 * <p>Chaque schéma tenant est géré par Flyway avec les scripts de
 * {@code db/migration/tenant/}. Avantage par rapport à l'exécution brute d'un script :
 * chaque schéma a son propre historique ({@code flyway_schema_history}), donc une nouvelle
 * migration (V3__..., V4__...) est appliquée automatiquement à TOUTES les auto-écoles
 * existantes au démarrage (voir {@code TenantMigrationRunner}).</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantProvisioningService {

    private static final String TENANT_MIGRATIONS = "classpath:db/migration/tenant";

    private final DataSource dataSource;
    private final TenantRepository tenantRepository;

    /**
     * Crée (ou met à jour) le schéma du tenant puis l'enregistre comme actif dans {@code public.tenants}.
     * Sans cette ligne dans {@code tenants}, {@link TenantService#isValidTenant} refuserait toutes les requêtes.
     */
    @Transactional
    public void createTenantSchema(String schema) {
        migrate(schema);

        TenantEntity tenant = tenantRepository.findByCode(schema).orElseGet(() -> new TenantEntity(schema));
        tenant.setActive(true);
        tenantRepository.save(tenant);
        log.info("Tenant {} provisionné et activé", schema);
    }

    /** Active / désactive un tenant (ex : suspension d'une auto-école). */
    @Transactional
    public void setActive(String schema, boolean active) {
        tenantRepository.findByCode(schema).ifPresent(tenant -> {
            tenant.setActive(active);
            tenantRepository.save(tenant);
        });
    }

    /** Applique les migrations en attente à tous les tenants connus (appelé au démarrage). */
    public void migrateAllTenants() {
        List<TenantEntity> tenants = tenantRepository.findAll();
        tenants.forEach(tenant -> migrate(tenant.getCode()));
        log.info("Migrations tenant appliquées sur {} schéma(s)", tenants.size());
    }

    private void migrate(String schema) {
        TenantSchemas.requireValid(schema);
        Flyway.configure()
                .dataSource(dataSource)
                .schemas(schema)              // crée le schéma s'il n'existe pas
                .defaultSchema(schema)        // les CREATE TABLE non qualifiés vont dans ce schéma
                .locations(TENANT_MIGRATIONS)
                // Schémas créés avant l'adoption de Flyway : tables déjà là, version 2 considérée appliquée.
                .baselineOnMigrate(true)
                .baselineVersion("2")
                .load()
                .migrate();
    }
}
