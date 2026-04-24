package cm.mvtech.drivehub.core.domain.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantProvisioningService {

    private final DataSource dataSource;

    // Lit le fichier SQL template depuis resources/
    @Value("classpath:db/migration/tenant/V2__init_tenant_schema_template.sql")
    private Resource tenantSchemaTemplate;

    @Transactional
    public void createTenantSchema(String tenantId) {

        String schema = tenantId.toLowerCase().replace("-", "_");

        // Lire le SQL du template une seule fois
        String templateSql;
        try {
            templateSql = tenantSchemaTemplate.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Impossible de lire le template SQL tenant", e);
        }

        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement()) {

            // 1. Créer le schéma (ton code existant)
            stmt.execute("CREATE SCHEMA IF NOT EXISTS " + schema);
            log.info("Schéma créé : {}", schema);

            // 2. Basculer vers le nouveau schéma
            stmt.execute("SET search_path TO " + schema);

            // 3. Créer toutes les tables métier dans ce schéma
            stmt.execute(templateSql);
            log.info("Tables métier créées dans le schéma : {}", schema);

            // 4. Revenir au schéma public
            stmt.execute("SET search_path TO public");

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Impossible de créer le schéma du tenant : " + schema, e);
        }
    }
}