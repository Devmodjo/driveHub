package cm.drivemaster.backend.models.dto;

import cm.drivemaster.backend.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record RegisterRequest(
        @NotBlank
        String name,

        @Email
        @NotBlank
        String email,

        /**
         * Mot de passe initial (peut être temporaire)
         */
        @NotBlank
        String password,

        /**
         * Rôle demandé
         */
        @NotNull
        Role role

) {
}
