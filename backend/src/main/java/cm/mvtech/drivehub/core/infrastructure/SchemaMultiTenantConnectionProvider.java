package cm.mvtech.drivehub.core.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
@Component
@RequiredArgsConstructor
public class SchemaMultiTenantConnectionProvider
        implements MultiTenantConnectionProvider {

    private static final String DEFAULT_SCHEMA = "public";

    private final DataSource dataSource;

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(Object tenantIdentifier)
            throws SQLException {

        final Connection connection = getAnyConnection();

        String schema = resolveSchema(tenantIdentifier);

        try (Statement statement = connection.createStatement()) {
            statement.execute("SET search_path TO " + schema);
            log.debug("Schéma PostgreSQL actif : {}", schema);
        } catch (SQLException e) {
            throw new HibernateException(
                    "Impossible de basculer vers le schéma [" + schema + "]", e
            );
        }

        return connection;
    }

    @Override
    public void releaseConnection(Object tenantIdentifier,
                                  Connection connection)
            throws SQLException {

        try (Statement statement = connection.createStatement()) {
            statement.execute("SET search_path TO " + DEFAULT_SCHEMA);
        } catch (SQLException e) {
            log.warn(
                    "Échec de la réinitialisation du schéma PostgreSQL",
                    e
            );
        }

        connection.close();
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        throw new UnsupportedOperationException(
                "Cette implémentation ne supporte pas unwrap()."
        );
    }

    /**
     * Résout le schéma PostgreSQL à utiliser.
     * - fallback vers 'public' si tenant null ou invalide
     */
    private String resolveSchema(Object tenantIdentifier) {
        if (tenantIdentifier == null) {
            return DEFAULT_SCHEMA;
        }

        String tenant = tenantIdentifier.toString();

        if (tenant.isBlank() || tenant.equalsIgnoreCase("default")) {
            return DEFAULT_SCHEMA;
        }

        return tenant;
    }
}
