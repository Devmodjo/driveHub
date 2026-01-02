package cm.drivemaster.backend.models.dto;

import cm.drivemaster.backend.enums.AdminRole;
import cm.drivemaster.backend.enums.AdminStatus;

public record PlatformAdminResponse(
        String name,
        String email,
        AdminRole role,
        AdminStatus status
) {
}
