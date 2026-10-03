package cm.mvtech.drivehub.modules.document.infrastructure.storage;

/**
 * Stockage des fichiers de justificatifs (contenu DÉJÀ chiffré par DataEncryptionService).
 *
 * <p>Deux implémentations, choisies par storage.provider dans application.yaml :</p>
 * <ul>
 *   <li>{@link LocalDocumentStorage} (LOCAL) : un dossier sur le disque, pour le développement et les tests ;</li>
 *   <li>{@link R2DocumentStorage} (R2) : Cloudflare R2, pour la production.</li>
 * </ul>
 * Le reste du code ne connaît que cette interface : changer de stockage ne demande aucune autre modification.
 *
 * <p>La clé d'un objet ({@code objectKey}) est générée par l'application (ex : documents/2026/10/uuid) :
 * elle ne contient jamais le nom de la personne ni le nom du fichier d'origine.</p>
 */
public interface DocumentStorage {

    void put(String objectKey, byte[] content);

    byte[] get(String objectKey);

    void delete(String objectKey);
}
