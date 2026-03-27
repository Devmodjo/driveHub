package cm.mvtech.drivehub.modules.messageapi;

import cm.mvtech.drivehub.modules.enums.Role;

import java.time.LocalDateTime;
import java.util.UUID;

public record PendingJoinRequestResponse(
        UUID requestId,
        UUID userId,
        String userName,
        String userEmail,
        Role role,
        UUID drivingSchoolId,
        String drivingSchoolName,
        LocalDateTime requestedAt
) {
}
