package cm.mvtech.drivehub.modules.document.infrastructure.storage;

import cm.mvtech.drivehub.modules.exception.ResourceNotFoundException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

/**
 * Justificatifs stockés dans la base PostgreSQL (table public.document_contents, migration V16).
 *
 * <p>Choix par défaut : rien d'autre à configurer pour héberger DriveHub. Les fichiers sont petits
 * (5 Mo maximum) et déjà chiffrés ; ils sont sauvegardés avec le reste de la base.
 * Pour un très grand nombre de fichiers, passer à Cloudflare R2 (STORAGE_PROVIDER=R2).</p>
 */
public class DatabaseDocumentStorage implements DocumentStorage {

    private final JdbcTemplate jdbc;
    /** Suppression dans sa propre transaction : elle est appelée APRÈS la validation de la transaction métier. */
    private final TransactionTemplate separateTransaction;

    public DatabaseDocumentStorage(JdbcTemplate jdbc, PlatformTransactionManager transactionManager) {
        this.jdbc = jdbc;
        this.separateTransaction = new TransactionTemplate(transactionManager);
        this.separateTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public void put(String objectKey, byte[] content) {
        jdbc.update("INSERT INTO public.document_contents (object_key, content) VALUES (?, ?) "
                + "ON CONFLICT (object_key) DO UPDATE SET content = EXCLUDED.content", objectKey, content);
    }

    @Override
    public byte[] get(String objectKey) {
        List<byte[]> rows = jdbc.query("SELECT content FROM public.document_contents WHERE object_key = ?",
                (rs, i) -> rs.getBytes(1), objectKey);
        if (rows.isEmpty()) {
            throw new ResourceNotFoundException("Fichier introuvable dans le stockage");
        }
        return rows.get(0);
    }

    @Override
    public void delete(String objectKey) {
        separateTransaction.executeWithoutResult(status ->
                jdbc.update("DELETE FROM public.document_contents WHERE object_key = ?", objectKey));
    }
}
