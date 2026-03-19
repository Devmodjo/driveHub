package cm.drivemaster.backend.models.dto;

import cm.drivemaster.backend.enums.AdminRole;
import cm.drivemaster.backend.enums.AdminStatus;

public record PlatformAdminAuthResponse(
        String token,
        AdminRole role,
        AdminStatus status
) {}
