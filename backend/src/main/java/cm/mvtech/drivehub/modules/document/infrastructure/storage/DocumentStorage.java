package cm.mvtech.drivehub.modules.document.infrastructure.storage;

/**
 * Stockage des fichiers de justificatifs (contenu DÉJÀ chiffré par DataEncryptionService).
 *
 * <p>Trois implémentations, choisies par storage.provider dans application.yaml :</p>
 * <ul>
 *   <li>{@link DatabaseDocumentStorage} (DATABASE, par défaut) : dans la base PostgreSQL, rien à configurer ;</li>
 *   <li>{@link R2DocumentStorage} (R2) : Cloudflare R2, pour un grand nombre de fichiers ;</li>
 *   <li>{@link LocalDocumentStorage} (LOCAL) : un dossier sur le disque.</li>
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
