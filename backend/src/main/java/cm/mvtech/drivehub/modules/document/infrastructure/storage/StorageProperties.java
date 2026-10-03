package cm.mvtech.drivehub.modules.document.infrastructure.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuration du stockage des justificatifs (application.yaml : storage.*). */
@ConfigurationProperties(prefix = "storage")
public record StorageProperties(String provider, Local local, R2 r2) {

    /** @param directory dossier racine des fichiers (développement) */
    public record Local(String directory) {
    }

    /**
     * Cloudflare R2 (tableau de bord Cloudflare → R2 → Manage R2 API Tokens).
     *
     * @param accountId       identifiant du compte Cloudflare
     * @param accessKeyId     clé d'accès du jeton API R2 (droits limités à ce bucket)
     * @param secretAccessKey secret du jeton API R2
     * @param bucket          nom du bucket (privé : aucun accès public)
     */
    public record R2(String accountId, String accessKeyId, String secretAccessKey, String bucket) {
    }
}
