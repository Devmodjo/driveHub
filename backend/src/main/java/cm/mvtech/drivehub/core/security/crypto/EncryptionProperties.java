package cm.mvtech.drivehub.core.security.crypto;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Clé maître de chiffrement (application.yaml : security.encryption.master-key, variable DATA_ENCRYPTION_KEY).
 *
 * @param masterKey 32 octets aléatoires encodés en Base64 (générés avec : openssl rand -base64 32)
 */
@ConfigurationProperties(prefix = "security.encryption")
public record EncryptionProperties(String masterKey) {
}
