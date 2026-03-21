package cm.mvtech.drivehub.modules.auth.application.dto;

import cm.mvtech.drivehub.modules.enums.Role;
import cm.mvtech.drivehub.modules.enums.ProfileStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.UUID;

public record UserResponseDto(

        @Schema(name = "1")
        UUID id,

        @Schema(name = "John")
        String name,

        @Schema(name = "john@example.com")
        String email,

        @Schema(name = "password123")
        String password,

        @Schema(name = "ADMIN")
        Role roles,

        @Schema(name = "PENDING")
        ProfileStatus profileStatus,

        LocalDate createdAt
) {
}
