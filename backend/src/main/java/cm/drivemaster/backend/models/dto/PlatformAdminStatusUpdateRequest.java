package cm.drivemaster.backend.models.dto;

import cm.drivemaster.backend.enums.AdminStatus;
import jakarta.validation.constraints.NotNull;

public record PlatformAdminStatusUpdateRequest(

        @NotNull
        AdminStatus status,

        boolean enabled

) {}
