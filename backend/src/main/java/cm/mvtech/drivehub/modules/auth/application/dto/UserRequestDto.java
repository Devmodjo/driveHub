package cm.mvtech.drivehub.modules.auth.application.dto;

import cm.mvtech.drivehub.modules.enums.Role;
import cm.mvtech.drivehub.modules.enums.ProfileStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record UserRequestDto(

        @Schema(name = "John")
        @NotBlank(message = "le nom d'utilisateur est obligatoire")
        String name,

        @Schema(name = "john@example.com")
        @Email(message = "format de l'adresse email est invalide")
        String email,

        @Schema(name = "password123")
        @NotBlank(message = "le mot de passe est obligatoire")
        String password,

        @Schema(name = "ADMIN")
        @NotBlank(message = "le role de l'utilisateur est obligatoire")
        Role roles,

        @Schema(name = "PENDING")
        ProfileStatus profileStatus,

        @Schema(name = "1")
        UUID userId
) {
}
