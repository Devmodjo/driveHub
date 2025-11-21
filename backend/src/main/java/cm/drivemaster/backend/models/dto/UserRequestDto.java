package cm.drivemaster.backend.models.dto;

import cm.drivemaster.backend.enums.Role;
import cm.drivemaster.backend.enums.Statut;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

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
        Statut statut

) {
}
