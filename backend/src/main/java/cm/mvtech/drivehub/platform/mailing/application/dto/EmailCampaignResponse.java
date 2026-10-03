package cm.mvtech.drivehub.platform.mailing.application.dto;

import cm.mvtech.drivehub.platform.mailing.domain.model.EmailAudience;

import java.time.LocalDateTime;
import java.util.UUID;

/** Ligne de l'historique des envois. */
public record EmailCampaignResponse(
        UUID id,
        String subject,
        String message,
        EmailAudience audience,
        String schoolName,
        int recipientCount,
        String sentByEmail,
        LocalDateTime sentAt
) {
}
