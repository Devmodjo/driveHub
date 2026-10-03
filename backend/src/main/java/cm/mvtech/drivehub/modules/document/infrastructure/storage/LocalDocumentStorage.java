package cm.mvtech.drivehub.modules.document.infrastructure.storage;

import cm.mvtech.drivehub.modules.exception.ResourceNotFoundException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.regex.Pattern;

/**
 * Stockage dans un dossier local (développement, tests). Ne pas utiliser en production :
 * le disque d'un serveur peut être perdu, et il n'est pas partagé entre plusieurs instances.
 */
public class LocalDocumentStorage implements DocumentStorage {

    /** Clés autorisées : générées par l'application, jamais par l'utilisateur (protection contre « ../ »). */
    private static final Pattern SAFE_KEY = Pattern.compile("^[a-z0-9][a-z0-9/_-]{0,200}$");

    private final Path root;

    public LocalDocumentStorage(String directory) {
        this.root = Path.of(directory).toAbsolutePath().normalize();
    }

    @Override
    public void put(String objectKey, byte[] content) {
        Path file = resolve(objectKey);
        try {
            Files.createDirectories(file.getParent());
            Files.write(file, content);
        } catch (IOException e) {
            throw new UncheckedIOException("Écriture du fichier impossible", e);
        }
    }

    @Override
    public byte[] get(String objectKey) {
        try {
            return Files.readAllBytes(resolve(objectKey));
        } catch (NoSuchFileException e) {
            throw new ResourceNotFoundException("Fichier introuvable dans le stockage");
        } catch (IOException e) {
            throw new UncheckedIOException("Lecture du fichier impossible", e);
        }
    }

    @Override
    public void delete(String objectKey) {
        try {
            Files.deleteIfExists(resolve(objectKey));
        } catch (IOException e) {
            throw new UncheckedIOException("Suppression du fichier impossible", e);
        }
    }

    private Path resolve(String objectKey) {
        if (objectKey == null || !SAFE_KEY.matcher(objectKey).matches() || objectKey.contains("..")) {
            throw new IllegalArgumentException("Clé de stockage invalide");
        }
        Path file = root.resolve(objectKey).normalize();
        if (!file.startsWith(root)) {
            throw new IllegalArgumentException("Clé de stockage invalide");
        }
        return file;
    }
}
