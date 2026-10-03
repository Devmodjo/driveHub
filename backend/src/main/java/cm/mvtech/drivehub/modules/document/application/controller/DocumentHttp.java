package cm.mvtech.drivehub.modules.document.application.controller;

import cm.mvtech.drivehub.modules.document.domain.model.DocumentFile;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Réponse HTTP d'un justificatif déchiffré, avec des en-têtes de sécurité :
 * <ul>
 *   <li>Content-Type d'origine (PDF ou image uniquement, vérifié à l'envoi) ;</li>
 *   <li>X-Content-Type-Options: nosniff — le navigateur n'essaie pas de deviner un autre type ;</li>
 *   <li>Cache-Control: no-store — aucune copie n'est gardée dans le cache du navigateur ou d'un proxy ;</li>
 *   <li>Content-Security-Policy « sandbox » — le fichier ne peut exécuter aucun script ;</li>
 *   <li>nom de fichier générique (cni.pdf, capec.jpg) — le nom d'origine n'est jamais exposé dans l'en-tête.</li>
 * </ul>
 */
public final class DocumentHttp {

    private DocumentHttp() {
    }

    public static ResponseEntity<byte[]> file(DocumentFile file) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(file.downloadName()).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .header("Content-Security-Policy", "default-src 'none'; img-src 'self' data:; style-src 'unsafe-inline'; sandbox")
                .cacheControl(CacheControl.noStore())
                .body(file.content());
    }
}
