package cm.drivemaster.backend.models.dto;

import cm.drivemaster.backend.enums.Role;

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
