package cm.mvtech.drivehub.core.security.crypto;

import java.util.Base64;

/** Service de chiffrement prêt à l'emploi pour les tests unitaires (clé fixe, jamais utilisée en production). */
public final class TestEncryption {

    private TestEncryption() {
    }

    public static DataEncryptionService service() {
        DataEncryptionService service = new DataEncryptionService(
                new EncryptionProperties(Base64.getEncoder().encodeToString(new byte[32])));
        service.init();
        return service;
    }
}
