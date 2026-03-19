package cm.mvtech.drivehub.modules.messageapi;

import cm.mvtech.drivehub.modules.enums.Role;

import java.time.LocalDateTime;

public record PendingJoinRequestResponse(
        Long requestId,
        Long userId,
        String userName,
        String userEmail,
        Role role,
        Long drivingSchoolId,
        String drivingSchoolName,
        LocalDateTime requestedAt
) {
}
