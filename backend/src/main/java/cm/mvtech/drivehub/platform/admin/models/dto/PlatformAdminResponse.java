package cm.mvtech.drivehub.platform.admin.models.dto;

import cm.mvtech.drivehub.platform.admin.enums.AdminRole;
import cm.mvtech.drivehub.platform.admin.enums.AdminStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record PlatformAdminResponse(
        UUID id,
        String name,
        String email,
        AdminRole role,
        AdminStatus adminStatus,
        LocalDateTime createdAt
) {
}
