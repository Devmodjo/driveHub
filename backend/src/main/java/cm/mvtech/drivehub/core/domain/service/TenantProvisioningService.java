package cm.mvtech.drivehub.core.domain.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * classe de creation automatique du schema par tenant
 */
@Service
@RequiredArgsConstructor
public class TenantProvisioningService {

    private final DataSource dataSource;

    @Transactional
    public void createTenantSchema(String tenantId) {

        String schema = tenantId.toLowerCase().replace("-", "_");

        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement()) {

            stmt.execute("CREATE SCHEMA IF NOT EXISTS " + schema);

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Impossible de créer le schéma du tenant : " + schema, e);
        }
    }
}
