package cm.mvtech.drivehub.modules.document.infrastructure.storage;

import cm.mvtech.drivehub.modules.exception.ResourceNotFoundException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Stockage Cloudflare R2 (production), via l'API compatible S3.
 *
 * <p>Le bucket doit rester PRIVÉ (pas de domaine public, pas d'URL signée) : les fichiers sont toujours
 * servis par l'API DriveHub, qui vérifie les droits, journalise la consultation et déchiffre le contenu.</p>
 */
public class R2DocumentStorage implements DocumentStorage {

    private final S3Client client;
    private final String bucket;

    public R2DocumentStorage(S3Client client, String bucket) {
        this.client = client;
        this.bucket = bucket;
    }

    @Override
    public void put(String objectKey, byte[] content) {
        client.putObject(PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(objectKey)
                        .contentType("application/octet-stream")   // contenu chiffré : jamais interprétable tel quel
                        .build(),
                RequestBody.fromBytes(content));
    }

    @Override
    public byte[] get(String objectKey) {
        try {
            return client.getObjectAsBytes(GetObjectRequest.builder().bucket(bucket).key(objectKey).build())
                    .asByteArray();
        } catch (NoSuchKeyException e) {
            throw new ResourceNotFoundException("Fichier introuvable dans le stockage");
        }
    }

    @Override
    public void delete(String objectKey) {
        client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(objectKey).build());
    }
}
