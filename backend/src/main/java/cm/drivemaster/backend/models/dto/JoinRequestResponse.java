package cm.drivemaster.backend.models.dto;

import cm.drivemaster.backend.enums.JoinStatus;
import cm.drivemaster.backend.enums.Role;

public record JoinRequestResponse(
        Long requestId,
        Long userId,
        String userName,
        String userEmail,
        Role requestedRole,
        JoinStatus joinStatus
) {
}
