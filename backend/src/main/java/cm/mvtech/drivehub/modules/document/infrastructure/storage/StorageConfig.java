package cm.mvtech.drivehub.modules.document.infrastructure.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

/**
 * Choix du stockage des justificatifs selon storage.provider : DATABASE (par défaut), R2 ou LOCAL.
 * <ul>
 *   <li>R2 incomplet (ex : en local, sans identifiants) : avertissement et repli sur la base, l'application démarre ;</li>
 *   <li>R2 ou LOCAL : les fichiers déjà enregistrés dans la base restent lisibles (FallbackDocumentStorage).</li>
 * </ul>
 */
@Slf4j
@Configuration
public class StorageConfig {

    @Bean
    public DocumentStorage documentStorage(StorageProperties properties, JdbcTemplate jdbcTemplate,
                                           PlatformTransactionManager transactionManager) {
        String provider = properties.provider() == null ? "DATABASE" : properties.provider().trim().toUpperCase();
        DocumentStorage database = new DatabaseDocumentStorage(jdbcTemplate, transactionManager);

        if ("R2".equals(provider)) {
            StorageProperties.R2 r2 = properties.r2();
            if (r2 == null || isBlank(r2.accountId()) || isBlank(r2.accessKeyId())
                    || isBlank(r2.secretAccessKey()) || isBlank(r2.bucket())) {
                // R2 demandé mais pas configuré (ex : en local) : on ne bloque pas le démarrage
                log.warn("STORAGE_PROVIDER=R2 mais R2_ACCOUNT_ID, R2_ACCESS_KEY_ID, R2_SECRET_ACCESS_KEY ou R2_BUCKET "
                        + "manque : les justificatifs sont stockés dans la base PostgreSQL.");
                return database;
            }
            S3Client client = S3Client.builder()
                    // Point d'accès R2 : https://<identifiant du compte>.r2.cloudflarestorage.com
                    .endpointOverride(URI.create("https://" + r2.accountId() + ".r2.cloudflarestorage.com"))
                    .region(Region.of("auto"))   // R2 n'a pas de régions : « auto »
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(r2.accessKeyId(), r2.secretAccessKey())))
                    .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                    .build();
            log.info("Justificatifs stockés sur Cloudflare R2 (bucket {}) ; les anciens restent lus dans la base", r2.bucket());
            // Nouveaux fichiers dans R2 ; ceux enregistrés avant dans la base restent lisibles
            return new FallbackDocumentStorage(new R2DocumentStorage(client, r2.bucket()), database);
        }
        if ("LOCAL".equals(provider)) {
            String directory = properties.local() == null || isBlank(properties.local().directory())
                    ? "./data/documents" : properties.local().directory();
            log.warn("Justificatifs stockés en LOCAL dans {} : à réserver au développement", directory);
            return new FallbackDocumentStorage(new LocalDocumentStorage(directory), database);
        }
        log.info("Justificatifs stockés dans la base PostgreSQL (chiffrés)");
        return database;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
