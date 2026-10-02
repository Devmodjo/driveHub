package cm.mvtech.drivehub.modules.messageapi;

import cm.mvtech.drivehub.modules.enums.JoinStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/** Demande d'adhésion vue par son auteur (élève ou moniteur). */
public record MyJoinRequestResponse(
        UUID requestId,
        UUID drivingSchoolId,
        String drivingSchoolName,
        String city,
        JoinStatus joinStatus,
        LocalDateTime requestedAt
) {
}
