package cm.mvtech.drivehub.platform.admin.models.dto;

import cm.mvtech.drivehub.platform.admin.enums.AdminRole;
import cm.mvtech.drivehub.platform.admin.enums.AdminStatus;

public record PlatformAdminAuthResponse(
        String token,
        AdminRole role,
        AdminStatus status
) {}
