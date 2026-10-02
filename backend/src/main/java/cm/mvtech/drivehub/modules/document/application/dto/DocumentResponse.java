package cm.mvtech.drivehub.modules.document.application.dto;

import cm.mvtech.drivehub.modules.document.domain.model.DocumentStatus;
import cm.mvtech.drivehub.modules.document.domain.model.DocumentType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Justificatif renvoyé par l'API. Le numéro n'est jamais renvoyé en entier (seuls les 4 derniers caractères).
 *
 * @param duplicateWarning vrai si le même numéro est utilisé par un autre compte (à examiner par le vérificateur)
 */
public record DocumentResponse(
        UUID id,
        DocumentType type,
        DocumentStatus status,
        String documentNumberMasked,
        String fileName,
        String contentType,
        long sizeBytes,
        LocalDateTime uploadedAt,
        String reviewComment,
        boolean duplicateWarning
) {
}
