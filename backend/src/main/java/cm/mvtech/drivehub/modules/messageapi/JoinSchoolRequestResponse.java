package cm.mvtech.drivehub.modules.messageapi;

import cm.mvtech.drivehub.modules.enums.JoinStatus;
import cm.mvtech.drivehub.modules.enums.Role;

public record JoinSchoolRequestResponse(
        Long requestId,
        Long userId,
        String userName,
        String userEmail,
        Role requestedRole,
        JoinStatus joinStatus
) {
}
