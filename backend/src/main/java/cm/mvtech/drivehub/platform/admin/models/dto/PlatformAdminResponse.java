package cm.mvtech.drivehub.platform.admin.models.dto;

import cm.mvtech.drivehub.platform.admin.enums.AdminRole;
import cm.mvtech.drivehub.platform.admin.enums.AdminStatus;

public record PlatformAdminResponse(
        String name,
        String email,
        AdminRole role,
        AdminStatus adminStatus
) {
}
