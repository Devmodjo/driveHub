package cm.mvtech.drivehub.modules.document.domain.model;

import java.util.Arrays;
import java.util.Optional;

/**
 * Formats de fichiers acceptés pour un justificatif, reconnus par leur SIGNATURE (les premiers octets),
 * et non par l'extension ou le type annoncé par le navigateur, qui peuvent être falsifiés.
 * Tout le reste (SVG, HTML, documents Office, exécutables...) est refusé.
 */
public enum AllowedFileType {

    PDF("application/pdf", "pdf"),
    JPEG("image/jpeg", "jpg"),
    PNG("image/png", "png"),
    WEBP("image/webp", "webp");

    private final String contentType;
    private final String extension;

    AllowedFileType(String contentType, String extension) {
        this.contentType = contentType;
        this.extension = extension;
    }

    public String contentType() {
        return contentType;
    }

    public String extension() {
        return extension;
    }

    /** Reconnaît le format réel du fichier à partir de ses premiers octets. */
    public static Optional<AllowedFileType> detect(byte[] bytes) {
        if (bytes == null || bytes.length < 12) {
            return Optional.empty();
        }
        if (startsWith(bytes, 0, 0x25, 0x50, 0x44, 0x46, 0x2D)) {                       // %PDF-
            return Optional.of(PDF);
        }
        if (startsWith(bytes, 0, 0xFF, 0xD8, 0xFF)) {                                   // JPEG
            return Optional.of(JPEG);
        }
        if (startsWith(bytes, 0, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)) {     // PNG
            return Optional.of(PNG);
        }
        if (startsWith(bytes, 0, 0x52, 0x49, 0x46, 0x46) && startsWith(bytes, 8, 0x57, 0x45, 0x42, 0x50)) { // RIFF....WEBP
            return Optional.of(WEBP);
        }
        return Optional.empty();
    }

    /** Type MIME d'un format accepté (pour renvoyer le fichier), ou empty si inconnu. */
    public static Optional<AllowedFileType> fromContentType(String contentType) {
        return Arrays.stream(values()).filter(t -> t.contentType.equals(contentType)).findFirst();
    }

    private static boolean startsWith(byte[] bytes, int offset, int... signature) {
        for (int i = 0; i < signature.length; i++) {
            if ((bytes[offset + i] & 0xFF) != signature[i]) {
                return false;
            }
        }
        return true;
    }
}
