package cm.mvtech.drivehub.platform.admin.models.dto;

import cm.mvtech.drivehub.platform.admin.enums.AdminRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateAdminRequest(
        @NotBlank String name,
        @Email @NotBlank String email,
        @NotNull AdminRole role,
        @NotBlank String residence,
        String phoneNumber
) {}