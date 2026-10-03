package cm.mvtech.drivehub.core.security.crypto;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

/**
 * Chiffrement des données sensibles (justificatifs d'identité, numéros de documents).
 *
 * <h2>Principe</h2>
 * <ul>
 *   <li><b>Algorithme</b> : AES-256-GCM. Il chiffre ET protège contre toute modification : un octet changé
 *       dans le texte chiffré fait échouer le déchiffrement (au lieu de renvoyer des données fausses).</li>
 *   <li><b>Clé maître</b> : DATA_ENCRYPTION_KEY (32 octets). On n'utilise jamais la clé maître directement :
 *       deux sous-clés en sont dérivées (HMAC-SHA256), une pour chiffrer, une pour les empreintes.
 *       Ainsi une même clé ne sert jamais à deux usages différents.</li>
 *   <li><b>Fichiers : chiffrement « enveloppe »</b>. Chaque fichier est chiffré avec sa propre clé aléatoire
 *       (clé de données), et seule cette clé de données est chiffrée avec la clé maître puis rangée en base.
 *       Le bucket R2 ne contient donc que des données illisibles, et la base ne contient pas les fichiers.</li>
 *   <li><b>Empreinte (« blind index »)</b> : HMAC du numéro de document normalisé. Elle permet de détecter
 *       qu'un même numéro de CNI ou de CAPEC est utilisé par deux comptes sans jamais le déchiffrer.</li>
 * </ul>
 *
 * <h2>Format d'une valeur chiffrée</h2>
 * {@code v1:} suivi de Base64( IV de 12 octets | texte chiffré | étiquette d'authentification de 16 octets ).
 * Le préfixe de version permettra de changer de clé ou d'algorithme plus tard sans casser les anciennes valeurs.
 *
 * <h2>Données associées (AAD)</h2>
 * Le chiffrement d'un fichier est « lié » à l'identifiant du document : un fichier chiffré ne peut pas être
 * échangé avec celui d'un autre document (le déchiffrement échouerait).
 */
@Service
@RequiredArgsConstructor
public class DataEncryptionService {

    private static final String VERSION = "v1";
    private static final String CIPHER = "AES/GCM/NoPadding";
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;
    private static final int KEY_BYTES = 32;

    private final EncryptionProperties properties;
    private final SecureRandom random = new SecureRandom();

    /** Sous-clé de chiffrement (dérivée de la clé maître). */
    private byte[] encryptionKey;
    /** Sous-clé des empreintes HMAC (dérivée de la clé maître). */
    private byte[] indexKey;

    /** Vérifie la clé au démarrage : l'application refuse de démarrer sans clé valide. */
    @PostConstruct
    void init() {
        String encoded = properties.masterKey();
        if (encoded == null || encoded.isBlank()) {
            throw new IllegalStateException("DATA_ENCRYPTION_KEY est absente. Générez-la une fois avec "
                    + "« openssl rand -base64 32 » et ajoutez-la aux variables d'environnement.");
        }
        byte[] master;
        try {
            master = Base64.getDecoder().decode(encoded.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("DATA_ENCRYPTION_KEY doit être encodée en Base64 (openssl rand -base64 32)");
        }
        if (master.length != KEY_BYTES) {
            throw new IllegalStateException("DATA_ENCRYPTION_KEY doit faire exactement 32 octets (reçu : "
                    + master.length + "). Générez-la avec « openssl rand -base64 32 ».");
        }
        encryptionKey = hmac(master, "drivehub-encryption-v1".getBytes(StandardCharsets.UTF_8));
        indexKey = hmac(master, "drivehub-blind-index-v1".getBytes(StandardCharsets.UTF_8));
        Arrays.fill(master, (byte) 0);
    }

    // ------------------------------------------------------------------ textes (colonnes de la base)

    /** Chiffre un texte avec la clé maître (null reste null). */
    public String encryptString(String plain) {
        if (plain == null) {
            return null;
        }
        return VERSION + ":" + Base64.getEncoder().encodeToString(
                encrypt(plain.getBytes(StandardCharsets.UTF_8), encryptionKey, null));
    }

    /** Déchiffre un texte produit par {@link #encryptString}. */
    public String decryptString(String stored) {
        if (stored == null) {
            return null;
        }
        return new String(decrypt(payload(stored), encryptionKey, null), StandardCharsets.UTF_8);
    }

    // ------------------------------------------------------------------ fichiers (chiffrement enveloppe)

    /** Nouvelle clé de données aléatoire (une par fichier). */
    public byte[] newDataKey() {
        byte[] key = new byte[KEY_BYTES];
        random.nextBytes(key);
        return key;
    }

    /** Chiffre la clé de données avec la clé maître, pour la ranger en base. */
    public String wrapDataKey(byte[] dataKey) {
        return VERSION + ":" + Base64.getEncoder().encodeToString(encrypt(dataKey, encryptionKey, null));
    }

    /** Retrouve la clé de données à partir de sa valeur rangée en base. */
    public byte[] unwrapDataKey(String wrapped) {
        return decrypt(payload(wrapped), encryptionKey, null);
    }

    /**
     * Chiffre le contenu d'un fichier.
     *
     * @param associatedData valeur liée au chiffrement (l'identifiant du document) : sans elle, déchiffrement impossible
     */
    public byte[] encryptFile(byte[] content, byte[] dataKey, String associatedData) {
        return encrypt(content, dataKey, associatedData.getBytes(StandardCharsets.UTF_8));
    }

    /** Déchiffre le contenu d'un fichier ; échoue si le fichier a été modifié ou échangé. */
    public byte[] decryptFile(byte[] encrypted, byte[] dataKey, String associatedData) {
        return decrypt(encrypted, dataKey, associatedData.getBytes(StandardCharsets.UTF_8));
    }

    // ------------------------------------------------------------------ empreintes

    /**
     * Empreinte HMAC-SHA256 (64 caractères hexadécimaux) d'une valeur normalisée : majuscules,
     * sans espaces ni tirets. « 1234 567-89 » et « 123456789 » ont la même empreinte.
     */
    public String blindIndex(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.toUpperCase(Locale.ROOT).replaceAll("[\\s\\-./]", "");
        return HexFormat.of().formatHex(hmac(indexKey, normalized.getBytes(StandardCharsets.UTF_8)));
    }

    // ------------------------------------------------------------------ outils

    private byte[] encrypt(byte[] plain, byte[] key, byte[] aad) {
        try {
            byte[] iv = new byte[IV_BYTES];
            random.nextBytes(iv);   // jamais deux fois le même IV avec la même clé
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(TAG_BITS, iv));
            if (aad != null) {
                cipher.updateAAD(aad);
            }
            byte[] encrypted = cipher.doFinal(plain);
            return ByteBuffer.allocate(IV_BYTES + encrypted.length).put(iv).put(encrypted).array();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Chiffrement impossible", e);
        }
    }

    private byte[] decrypt(byte[] data, byte[] key, byte[] aad) {
        if (data.length < IV_BYTES + TAG_BITS / 8) {
            throw new IllegalStateException("Donnée chiffrée invalide (trop courte)");
        }
        try {
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"),
                    new GCMParameterSpec(TAG_BITS, data, 0, IV_BYTES));
            if (aad != null) {
                cipher.updateAAD(aad);
            }
            return cipher.doFinal(data, IV_BYTES, data.length - IV_BYTES);
        } catch (GeneralSecurityException e) {
            // Mauvaise clé, donnée modifiée ou fichier échangé : on ne renvoie jamais de données douteuses
            throw new IllegalStateException("Déchiffrement impossible : donnée altérée ou clé incorrecte", e);
        }
    }

    private static byte[] payload(String stored) {
        if (!stored.startsWith(VERSION + ":")) {
            throw new IllegalStateException("Format de donnée chiffrée inconnu");
        }
        return Base64.getDecoder().decode(stored.substring(VERSION.length() + 1));
    }

    private static byte[] hmac(byte[] key, byte[] data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(data);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC indisponible", e);
        }
    }
}
