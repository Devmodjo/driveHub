package cm.mvtech.drivehub.platform.admin.models.dto;

import cm.mvtech.drivehub.platform.admin.enums.AdminStatus;
import jakarta.validation.constraints.NotNull;

public record PlatformAdminStatusUpdateRequest(

        @NotNull
        AdminStatus status,

        boolean enabled

) {}
