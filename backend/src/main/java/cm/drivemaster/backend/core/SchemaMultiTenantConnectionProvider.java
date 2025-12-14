package cm.drivemaster.backend.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * ============================================================================
 *  SchemaMultiTenantConnectionProvider
 * ============================================================================
 *  RÔLE DE LA CLASSE
 *  -----------------
 *  Cette classe est le fournisseur de connexions JDBC multi-tenant utilisé
 *  par Hibernate dans une architecture SaaS basée sur la stratégie
 *  de multi-tenancy par schéma (SCHEMA).
 *  Elle permet à Hibernate :
 *   - d’obtenir une connexion JDBC générique au démarrage,
 *   - de basculer dynamiquement le schéma SQL actif en fonction du tenant,
 *   - de garantir l’isolation des données entre les clients,
 *   - de sécuriser la réutilisation des connexions via le pool.
 *  CONTEXTE D’UTILISATION
 *  ---------------------
 *  - Une seule base de données
 *  - Un schéma SQL par tenant
 *  - Une application Spring Boot (Hibernate 6+)
 *  Cette classe est appelée automatiquement par Hibernate à chaque
 *  ouverture et libération de session.
 * ============================================================================
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SchemaMultiTenantConnectionProvider
        implements MultiTenantConnectionProvider {

    /**
     * DataSource principal de l'application.
     * Il pointe vers une base de données unique contenant tous les schémas
     * des différents tenants.
     */
    private final DataSource dataSource;

    /**
     * Fournit une connexion JDBC générique, indépendante de tout tenant.
     *
     * Cette méthode est utilisée par Hibernate lors de son initialisation
     * interne (bootstrap).
     *
     * @return une connexion JDBC standard
     * @throws SQLException en cas d’erreur d’accès à la base
     */
    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Libère une connexion générique précédemment obtenue.
     *
     * @param connection connexion JDBC à libérer
     * @throws SQLException en cas d’erreur lors de la fermeture
     */
    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    /**
     * Fournit une connexion JDBC spécifique à un tenant donné.
     *
     * Hibernate transmet ici l’identifiant du tenant résolu en amont
     * (via CurrentTenantIdentifierResolver).
     *
     * Le schéma SQL actif est alors modifié dynamiquement afin que toutes
     * les requêtes exécutées sur cette connexion ciblent exclusivement
     * les données du tenant concerné.
     *
     * @param tenantIdentifier identifiant du tenant (généralement une String)
     * @return connexion JDBC configurée pour le tenant
     * @throws SQLException en cas d’erreur SQL
     */
    @Override
    public Connection getConnection(Object tenantIdentifier)
            throws SQLException {

        final Connection connection = getAnyConnection();

        try (Statement statement = connection.createStatement()) {
            statement.execute("USE " + tenantIdentifier);
        } catch (SQLException e) {
            throw new HibernateException(
                    "Impossible de basculer vers le schéma [" + tenantIdentifier + "]", e
            );
        }

        return connection;
    }

    /**
     * Libère une connexion JDBC spécifique à un tenant.
     * Avant de fermer la connexion, le schéma SQL est réinitialisé
     * vers un schéma par défaut afin d’éviter toute fuite de contexte
     * lors de la réutilisation de la connexion par le pool.
     *
     * @param tenantIdentifier identifiant du tenant
     * @param connection connexion JDBC à libérer
     * @throws SQLException en cas d’erreur lors de la fermeture
     */
    @Override
    public void releaseConnection(Object tenantIdentifier,
                                  Connection connection)
            throws SQLException {

        try (Statement statement = connection.createStatement()) {
            statement.execute("USE public");
        } catch (SQLException e) {
            log.warn(
                    "Échec de la réinitialisation du schéma pour le tenant [{}]",
                    tenantIdentifier,
                    e
            );
        }

        connection.close();
    }

    /**
     * Indique si Hibernate peut libérer agressivement les connexions.
     *
     * Dans un contexte multi-tenant avec pool de connexions, cette option
     * est généralement désactivée afin d’éviter des comportements
     * imprévisibles.
     *
     * @return false
     */
    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    /**
     * Indique si cette implémentation peut être "unwrapée"
     * vers un autre type.
     *
     * @param unwrapType type cible
     * @return false
     */
    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return false;
    }

    /**
     * Méthode interne Hibernate permettant d’accéder à une implémentation
     * spécifique.
     *
     * @param unwrapType type cible
     * @param <T> type retourné
     * @return jamais retourné
     */
    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        throw new UnsupportedOperationException(
                "Cette implémentation ne supporte pas unwrap()."
        );
    }
}
