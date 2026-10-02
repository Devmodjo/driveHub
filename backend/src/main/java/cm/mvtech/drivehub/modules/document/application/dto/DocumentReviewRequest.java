package cm.mvtech.drivehub.modules.document.application.dto;

import cm.mvtech.drivehub.modules.document.domain.model.DocumentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Décision du vérificateur sur un justificatif (commentaire obligatoire en cas de refus). */
public record DocumentReviewRequest(
        @NotNull(message = "Indiquez la décision : VERIFIED ou REJECTED")
        DocumentStatus status,
        @Size(max = 500, message = "Le commentaire ne doit pas dépasser 500 caractères")
        String comment
) {
}
