package cm.mvtech.drivehub.modules.document.infrastructure.storage;

import cm.mvtech.drivehub.modules.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;

/**
 * Passage en douceur d'un stockage à un autre (ex : de la base PostgreSQL vers Cloudflare R2).
 *
 * <ul>
 *   <li>les nouveaux fichiers vont dans le stockage principal ({@code primary}, ex : R2) ;</li>
 *   <li>un fichier absent du principal est cherché dans l'ancien ({@code fallback}, ex : la base) :
 *       les justificatifs envoyés avant le changement restent lisibles, sans migration ;</li>
 *   <li>une suppression est faite dans les deux.</li>
 * </ul>
 */
@Slf4j
public class FallbackDocumentStorage implements DocumentStorage {

    private final DocumentStorage primary;
    private final DocumentStorage fallback;

    public FallbackDocumentStorage(DocumentStorage primary, DocumentStorage fallback) {
        this.primary = primary;
        this.fallback = fallback;
    }

    @Override
    public void put(String objectKey, byte[] content) {
        primary.put(objectKey, content);
    }

    @Override
    public byte[] get(String objectKey) {
        try {
            return primary.get(objectKey);
        } catch (ResourceNotFoundException e) {
            return fallback.get(objectKey);   // fichier enregistré avant le changement de stockage
        }
    }

    @Override
    public void delete(String objectKey) {
        primary.delete(objectKey);
        try {
            fallback.delete(objectKey);
        } catch (RuntimeException e) {
            log.debug("Suppression dans l'ancien stockage ignorée pour {} : {}", objectKey, e.getMessage());
        }
    }
}
