package cm.mvtech.drivehub.modules.document.infrastructure.storage;

import cm.mvtech.drivehub.modules.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class LocalDocumentStorageTest {

    @TempDir
    Path directory;

    @Test
    void putGetDelete() {
        LocalDocumentStorage storage = new LocalDocumentStorage(directory.toString());
        storage.put("documents/2026/10/abc", new byte[]{1, 2, 3});
        assertTrue(Files.exists(directory.resolve("documents/2026/10/abc")));
        assertArrayEquals(new byte[]{1, 2, 3}, storage.get("documents/2026/10/abc"));

        storage.delete("documents/2026/10/abc");
        assertThrows(ResourceNotFoundException.class, () -> storage.get("documents/2026/10/abc"));
    }

    @Test
    void unsafeKeys_ShouldBeRefused() {
        LocalDocumentStorage storage = new LocalDocumentStorage(directory.toString());
        for (String key : new String[]{"../secret", "documents/../../etc/passwd", "/etc/passwd", "Documents/A", "", null}) {
            assertThrows(IllegalArgumentException.class, () -> storage.put(key, new byte[]{1}), "clé : " + key);
        }
    }
}
