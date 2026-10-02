package cm.mvtech.drivehub.core.security.crypto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/** Chiffrement AES-256-GCM des textes et fichiers, et empreintes HMAC des numéros de documents. */
class DataEncryptionServiceTest {

    private static final String KEY = Base64.getEncoder().encodeToString(new byte[32]);
    private static final String OTHER_KEY = Base64.getEncoder().encodeToString("abcdefghijklmnopqrstuvwxyz012345".getBytes());

    private DataEncryptionService service;

    @BeforeEach
    void setUp() {
        service = withKey(KEY);
    }

    private static DataEncryptionService withKey(String key) {
        DataEncryptionService s = new DataEncryptionService(new EncryptionProperties(key));
        s.init();
        return s;
    }

    @Test
    void encryptString_RoundTrip_AndCiphertextIsVersionedAndRandomized() {
        String a = service.encryptString("AB123456");
        String b = service.encryptString("AB123456");
        assertTrue(a.startsWith("v1:"));
        assertNotEquals(a, b, "un IV aléatoire : deux chiffrements du même texte diffèrent");
        assertFalse(a.contains("AB123456"));
        assertEquals("AB123456", service.decryptString(a));
        assertNull(service.encryptString(null));
        assertNull(service.decryptString(null));
    }

    @Test
    void decryptString_TamperedValue_ShouldFail() {
        String stored = service.encryptString("secret");
        byte[] raw = Base64.getDecoder().decode(stored.substring(3));
        raw[raw.length - 1] ^= 1;   // modifie le tag d'authentification
        String tampered = "v1:" + Base64.getEncoder().encodeToString(raw);
        assertThrows(IllegalStateException.class, () -> service.decryptString(tampered));
    }

    @Test
    void decryptString_WithAnotherMasterKey_ShouldFail() {
        String stored = service.encryptString("secret");
        assertThrows(IllegalStateException.class, () -> withKey(OTHER_KEY).decryptString(stored));
    }

    @Test
    void encryptFile_RoundTrip_WithWrappedDataKey() {
        byte[] content = "contenu du PDF".getBytes(StandardCharsets.UTF_8);
        byte[] dataKey = service.newDataKey();
        byte[] encrypted = service.encryptFile(content, dataKey, "documents/2026/10/a");
        String wrapped = service.wrapDataKey(dataKey);

        assertNotEquals(new String(content), new String(encrypted, StandardCharsets.ISO_8859_1));
        assertArrayEquals(content, service.decryptFile(encrypted, service.unwrapDataKey(wrapped), "documents/2026/10/a"));
    }

    @Test
    void decryptFile_WithAnotherObjectKey_ShouldFail() {
        byte[] dataKey = service.newDataKey();
        byte[] encrypted = service.encryptFile(new byte[]{1, 2, 3}, dataKey, "documents/a");
        // un fichier chiffré ne peut pas être « déplacé » vers un autre document
        assertThrows(IllegalStateException.class, () -> service.decryptFile(encrypted, dataKey, "documents/b"));
    }

    @Test
    void blindIndex_IsStableAndNormalized() {
        assertEquals(service.blindIndex("ab-123 456"), service.blindIndex("AB123456"));
        assertNotEquals(service.blindIndex("AB123456"), service.blindIndex("AB123457"));
        assertNotEquals(service.blindIndex("AB123456"), withKey(OTHER_KEY).blindIndex("AB123456"));
        assertNull(service.blindIndex(null));
        assertFalse(service.blindIndex("AB123456").contains("123456"));
    }

    @Test
    void init_InvalidKeys_ShouldRefuseToStart() {
        assertThrows(IllegalStateException.class, () -> withKey(null));
        assertThrows(IllegalStateException.class, () -> withKey(""));
        assertThrows(IllegalStateException.class, () -> withKey("pas du base64 !"));
        assertThrows(IllegalStateException.class, () -> withKey(Base64.getEncoder().encodeToString(new byte[16])));
    }
}
