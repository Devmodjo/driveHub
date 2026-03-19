package cm.mvtech.drivehub.modules.messageapi;

import cm.mvtech.drivehub.modules.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record JoinSchoolRequestDto(
        @NotNull Long drivingSchoolId,
        @Schema(defaultValue = "STUDENT")
        @NotNull Role role
) {}
