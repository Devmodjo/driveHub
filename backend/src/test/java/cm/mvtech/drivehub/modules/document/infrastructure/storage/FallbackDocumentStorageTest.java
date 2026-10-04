package cm.mvtech.drivehub.modules.document.infrastructure.storage;

import cm.mvtech.drivehub.modules.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/** Passage de la base à R2 : nouveaux fichiers dans R2, anciens toujours lisibles ; R2 absent = la base. */
class FallbackDocumentStorageTest {

    /** Stockage en mémoire pour les tests. */
    static class MemoryStorage implements DocumentStorage {
        final Map<String, byte[]> files = new HashMap<>();
        @Override public void put(String key, byte[] content) { files.put(key, content); }
        @Override public byte[] get(String key) {
            byte[] content = files.get(key);
            if (content == null) throw new ResourceNotFoundException("absent");
            return content;
        }
        @Override public void delete(String key) { files.remove(key); }
    }

    @Test
    void newFilesGoToPrimary_OldFilesAreReadFromFallback() {
        MemoryStorage r2 = new MemoryStorage();
        MemoryStorage database = new MemoryStorage();
        database.put("documents/ancien", new byte[]{1});
        FallbackDocumentStorage storage = new FallbackDocumentStorage(r2, database);

        storage.put("documents/nouveau", new byte[]{2});
        assertArrayEquals(new byte[]{2}, r2.get("documents/nouveau"));
        assertFalse(database.files.containsKey("documents/nouveau"));

        assertArrayEquals(new byte[]{1}, storage.get("documents/ancien"), "fichier d'avant le changement");
        assertThrows(ResourceNotFoundException.class, () -> storage.get("documents/inconnu"));

        storage.delete("documents/ancien");
        assertFalse(database.files.containsKey("documents/ancien"));
    }

    @Test
    void storageConfig_R2WithoutCredentials_FallsBackToDatabase() {
        StorageConfig config = new StorageConfig();
        DocumentStorage storage = config.documentStorage(
                new StorageProperties("R2", null, new StorageProperties.R2("", "", "", "drivehub-documents")),
                mock(JdbcTemplate.class), mock(PlatformTransactionManager.class));
        assertInstanceOf(DatabaseDocumentStorage.class, storage, "le démarrage n'est jamais bloqué par R2 absent");
    }

    @Test
    void storageConfig_R2Configured_KeepsDatabaseAsFallback() {
        StorageConfig config = new StorageConfig();
        DocumentStorage storage = config.documentStorage(
                new StorageProperties("R2", null, new StorageProperties.R2("compte", "cle", "secret", "drivehub-documents")),
                mock(JdbcTemplate.class), mock(PlatformTransactionManager.class));
        assertInstanceOf(FallbackDocumentStorage.class, storage);
    }

    @Test
    void storageConfig_DefaultIsDatabase() {
        DocumentStorage storage = new StorageConfig().documentStorage(new StorageProperties(null, null, null),
                mock(JdbcTemplate.class), mock(PlatformTransactionManager.class));
        assertInstanceOf(DatabaseDocumentStorage.class, storage);
    }
}
