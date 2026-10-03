package cm.mvtech.drivehub.platform.mailing.application.dto;

import java.util.UUID;

/** Résultat d'un envoi : l'envoi réel se fait en arrière-plan, un par un. */
public record SendPlatformEmailResponse(UUID id, int recipientCount, String message) {
}
