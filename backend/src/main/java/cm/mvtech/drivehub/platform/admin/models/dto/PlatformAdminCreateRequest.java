package cm.mvtech.drivehub.platform.admin.models.dto;

import cm.mvtech.drivehub.platform.admin.enums.AdminRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 *  creation d'un admin par le root via le back-office
 * @param name
 * @param email
 * @param role
 * @param password
 * @param residence
 * @param phoneNumber
 */
public record PlatformAdminCreateRequest(

        @NotBlank
        String name,

        @Email
        @NotBlank
        String email,

        @NotNull
        AdminRole role,

        @NotBlank
        String password,

        @NotBlank
        String residence,

        String phoneNumber

) {}
