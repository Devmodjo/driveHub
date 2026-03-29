package cm.mvtech.drivehub.modules.auth.application.dto;

import cm.mvtech.drivehub.modules.enums.ProfileStatus;
import cm.mvtech.drivehub.modules.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.UUID;

public record CurrentUserResponse(
        UUID id,

        @Schema(name = "John")
        String firstname,

        String lastname,

        @Schema(name = "john@example.com")
        String email,

//        @Schema(name = "password123")
//        String password,

        @Schema(name = "ADMIN")
        Role roles,

        @Schema(name = "PENDING")
        ProfileStatus profileStatus,

        LocalDate createdAt,

        @Schema(name= "true")
        Boolean fullprofile
) {
}
