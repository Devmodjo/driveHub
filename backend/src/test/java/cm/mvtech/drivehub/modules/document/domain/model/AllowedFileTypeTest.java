package cm.mvtech.drivehub.modules.document.domain.model;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/** Le format est reconnu sur le contenu réel du fichier, pas sur son nom. */
class AllowedFileTypeTest {

    private static byte[] withHeader(int... header) {
        byte[] bytes = new byte[32];
        for (int i = 0; i < header.length; i++) {
            bytes[i] = (byte) header[i];
        }
        return bytes;
    }

    @Test
    void detect_KnownSignatures() {
        assertEquals(Optional.of(AllowedFileType.PDF), AllowedFileType.detect("%PDF-1.7 ....".getBytes(StandardCharsets.US_ASCII)));
        assertEquals(Optional.of(AllowedFileType.JPEG), AllowedFileType.detect(withHeader(0xFF, 0xD8, 0xFF, 0xE0)));
        assertEquals(Optional.of(AllowedFileType.PNG), AllowedFileType.detect(withHeader(0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)));
        assertEquals(Optional.of(AllowedFileType.WEBP), AllowedFileType.detect(withHeader('R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P')));
    }

    @Test
    void detect_RejectsOtherContent() {
        assertTrue(AllowedFileType.detect("<html><script>".getBytes(StandardCharsets.US_ASCII)).isEmpty());
        assertTrue(AllowedFileType.detect("MZ executable....".getBytes(StandardCharsets.US_ASCII)).isEmpty());
        assertTrue(AllowedFileType.detect(new byte[3]).isEmpty());
        assertTrue(AllowedFileType.detect(null).isEmpty());
        byte[] riffNotWebp = withHeader('R', 'I', 'F', 'F', 0, 0, 0, 0, 'A', 'V', 'I', ' ');
        assertTrue(AllowedFileType.detect(riffNotWebp).isEmpty());
        assertTrue(AllowedFileType.detect(Arrays.copyOf(new byte[]{'%', 'P', 'D'}, 20)).isEmpty());
    }

    @Test
    void fromContentType() {
        assertEquals(Optional.of(AllowedFileType.PNG), AllowedFileType.fromContentType("image/png"));
        assertTrue(AllowedFileType.fromContentType("text/html").isEmpty());
    }
}
