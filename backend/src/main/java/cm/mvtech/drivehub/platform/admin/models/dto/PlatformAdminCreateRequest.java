package cm.mvtech.drivehub.platform.admin.models.dto;

import cm.mvtech.drivehub.platform.admin.enums.AdminRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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

        @NotBlank
        String phoneNumber,

        @Schema(defaultValue = "décrivez brievement votre motivtion. 300 caractères min")
        @Size(max = 300, message = "Veuillez détailler votre motivation (300 caractères minimum)")
        String reason
) {}
